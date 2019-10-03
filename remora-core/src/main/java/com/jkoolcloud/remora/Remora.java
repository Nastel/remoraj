package com.jkoolcloud.remora;

import static com.jkoolcloud.remora.core.utils.LoggerWrapper.pLog;

import java.io.File;
import java.io.FileFilter;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.jkoolcloud.remora.core.output.OutputManager;
import com.jkoolcloud.remora.core.utils.RemoraClassLoader;

public class Remora {

	public static final boolean DEBUG_BOOT_LOADER = true;
	public static final String MODULES_DIR = "/modules";
	public static final String REMORA_PATH = "remora.path";
	private static URLClassLoader bootLoader;

	public Remora() {
		System.out.println("App loaded with classloader: " + getClass().getClassLoader());
	}

	public static void main(String[] args) throws Exception {
		System.out.println("Starting from main");
		System.out.println("This application intended to be used as javaAgent.");
	}

	public static void premain(String options, Instrumentation inst) throws Exception {
		pLog("Starting from premain; classloader: " + Remora.class.getClassLoader());
		if (System.getProperty(REMORA_PATH) == null) {
			System.setProperty(REMORA_PATH, options);
		}
		OutputManager outputManager = OutputManager.INSTANCE; //
		RemoraConfig remoraConfig = RemoraConfig.INSTANCE; // Load output and config manager by Bootstarp classloader;
		bootLoader = new RemoraClassLoader(findJars(options + MODULES_DIR), Remora.class.getClassLoader(), inst);
		pLog("Initializing classloader: " + bootLoader);
		Class<?> appClass = bootLoader.loadClass(RemoraInit.class.getName());
		Object instance = appClass.newInstance();
		pLog("Initializing agent class: " + appClass);
		Method initializeAdvices = appClass.getMethod("initializeAdvices", Instrumentation.class, ClassLoader.class);
		pLog("Initializing advices: " + appClass);
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
				pLog("No modules found");
				return null;
			}
			List<File> files = Arrays.asList(modulesFiles);

			URL[] uris = files.stream().map(file -> {
				try {
					return file.toURI().toURL();
				} catch (MalformedURLException e) {
					return null;
				}
			}).peek(file -> pLog("Found: " + file)).collect(Collectors.toList()).toArray(new URL[files.size()]);

			return uris;
		} else {
			pLog("Module dir is not existing; Check java agent parameters; --javaagent:remora.jar=parameters. Parameters should point to the directory remora exists");
			return null;
		}
	}

}
