package com.jkoolcloud.remora;

import static java.text.MessageFormat.format;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.jar.JarFile;
import java.util.logging.*;
import java.util.stream.Collectors;

import com.jkoolcloud.remora.core.output.OutputManager;
import com.jkoolcloud.remora.core.utils.RemoraClassLoader;

public class Remora {

	public static Logger logger;

	private static final Level DEFAULT_LEVEL = Level.FINEST;
	public static final int LOG_FILE_SIZE = 1024;
	public static final int LOG_COUNT = 1;

	public static final SimpleFormatter REMORA_LOG_FORMATTER = new SimpleFormatter() {
		private static final String format = "[%1$tF %1$tT] [%2$-7s] %3$s %n";

		@Override
		public synchronized String format(LogRecord lr) {
			return String.format(format, new Date(lr.getMillis()), lr.getLevel().getLocalizedName(), lr.getMessage());
		}
	};

	public static final boolean DEBUG_BOOT_LOADER = true;
	public static final String MODULES_DIR = "/modules";
	public static final String REMORA_PATH = "remora.path";
	public static final String MAIN_LOG = "/log/" + "remora%u.log";
	private static URLClassLoader bootLoader;

	public Remora() {
		logger.info("App loaded with classloader: " + getClass().getClassLoader());
	}

	public static void main(String[] args) throws Exception {
		System.out.println("Starting from main");
		System.out.println("This application intended to be used as javaAgent.");
	}

	public static void premain(String options, Instrumentation inst) throws Exception {
		if (System.getProperty(REMORA_PATH) == null) {
			System.setProperty(REMORA_PATH, options);
		}
		String baseRemoraDir = System.getProperty(Remora.REMORA_PATH);

		configureRemoraRootLogger(baseRemoraDir);
		logger = Logger.getLogger(Remora.class.getName());
		logger.info("Starting from premain; classloader: " + Remora.class.getClassLoader());

		OutputManager outputManager = OutputManager.INSTANCE; //
		RemoraConfig remoraConfig = RemoraConfig.INSTANCE; // Load output and config manager by Bootstarp classloader;
		inst.appendToBootstrapClassLoaderSearch(new JarFile(baseRemoraDir + "remora.jar"));
		bootLoader = new RemoraClassLoader(findJars(options + MODULES_DIR), Remora.class.getClassLoader(), inst);
		logger.info("Initializing classloader: " + bootLoader);
		Class<?> appClass = bootLoader.loadClass("com.jkoolcloud.remora.RemoraInit");
		Object instance = appClass.newInstance();
		logger.info("Initializing agent class: " + appClass);
		Method initializeAdvices = appClass.getMethod("initializeAdvices", Instrumentation.class, ClassLoader.class);
		// logger.info("Initializing advices: " + appClass);
		initializeAdvices.invoke(instance, inst, bootLoader);
	}

	public static URL[] findJars(String location) throws MalformedURLException {
		File moduleFolder = new File(location);

		if (moduleFolder.exists()) {

			File[] modulesFiles = moduleFolder.listFiles(new FileFilter() {
				@Override
				public boolean accept(File pathname) {
					String name = pathname.getName();
					return name.endsWith(".jar") || name.endsWith(".zip");
				}
			});
			if (modulesFiles == null) {
				logger.severe("No modules found");
				return null;
			}
			List<File> files = Arrays.asList(modulesFiles);

			URL[] uris = files.stream().map(file -> {
				try {
					return file.toURI().toURL();
				} catch (MalformedURLException e) {
					return null;
				}
			}).peek(file -> logFoundClasspathEntry(file)).collect(Collectors.toList()).toArray(new URL[files.size()]);

			return uris;
		} else {
			logger.severe(
					"Module dir is not existing; Check java agent parameters; --javaagent:remora.jar=parameters. Parameters should point to the directory remora exists");
			return null;
		}
	}

	private static void logFoundClasspathEntry(URL file) {
		if (logger == null) {
			System.out.println("Found: " + file);
		} else {
			logger.info("Found: " + file);
		}
	}

	protected static void configureRemoraRootLogger(String baseRemoraDir) {
		Logger remoraLogger = Logger.getLogger("com.jkoolcloud.remora");
		String REMORA_LOG = baseRemoraDir + MAIN_LOG;

		try {
			new File(REMORA_LOG).getParentFile().mkdirs();
			FileHandler handler = new FileHandler(REMORA_LOG, LOG_FILE_SIZE, LOG_COUNT, true);
			handler.setLevel(Level.parse(RemoraConfig.INSTANCE.config.getProperty("remora", DEFAULT_LEVEL.getName())));
			handler.setFormatter(REMORA_LOG_FORMATTER);
			handler.setLevel(Level.ALL);
			remoraLogger.addHandler(handler);
			remoraLogger.addHandler(new StreamHandler(System.out, Remora.REMORA_LOG_FORMATTER));
			remoraLogger.info(format("Root logger configured, level {1}, handlers {2},  ", remoraLogger,
					remoraLogger.getLevel(), remoraLogger.getHandlers()));

		} catch (IOException e) {
			remoraLogger
					.info(format("Exception: {0} {1} \n {2}", Remora.class.getName(), "configureRemoraRootLogger", e));
			e.printStackTrace();
		}
	}

}
