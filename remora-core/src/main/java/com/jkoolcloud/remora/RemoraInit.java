package com.jkoolcloud.remora;

import static java.text.MessageFormat.format;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.util.Arrays;
import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.logging.*;

import com.jkoolcloud.remora.advices.RemoraAdvice;

public class RemoraInit {

	public static final int LOG_FILE_SIZE = 1024;
	public static final int LOG_COUNT = 1;

	private static final Logger LOGGER = Logger.getLogger(RemoraInit.class.getName());
	public static final String REMORA_LOG = System.getProperty(Remora.REMORA_PATH) + "/log/" + "remora%u.log";
	private static final Level DEFAULT_LEVEL = Level.INFO;
	public static final SimpleFormatter REMORA_LOG_FORMATTER = new SimpleFormatter();

	public void initializeAdvices(Instrumentation inst, ClassLoader classLoader) {
		configureRemoraRootLogger();

		LOGGER.info("Initializing advices: " + getClass() + " classloader: " + getClass().getClassLoader());

		ServiceLoader<RemoraAdvice> advices = ServiceLoader.load(RemoraAdvice.class, classLoader);

		Iterator<RemoraAdvice> iterator = advices.iterator();
		while (iterator.hasNext()) {
			RemoraAdvice remoraAdvice = iterator.next();
			RemoraConfig.INSTANCE.configure(remoraAdvice);
			remoraAdvice.install(inst);
			configureAdviceLogger(remoraAdvice);

			LOGGER.info("\t Found module: " + remoraAdvice);

		}
		LOGGER.info("Loading finished");

	}

	protected void configureRemoraRootLogger() {
		Logger remoraLogger = LOGGER.getParent();
		try {
			new File(REMORA_LOG).getParentFile().mkdirs();
			FileHandler handler = new FileHandler(REMORA_LOG, LOG_FILE_SIZE, LOG_COUNT, true);
			handler.setLevel(Level.parse(RemoraConfig.INSTANCE.config.getProperty("remora", DEFAULT_LEVEL.getName())));
			handler.setFormatter(REMORA_LOG_FORMATTER);
			handler.setLevel(Level.ALL);
			remoraLogger.addHandler(handler);
		} catch (IOException e) {
			LOGGER.throwing(getClass().getName(), "configureRemoraRootLogger", e);
		}
	}

	protected void configureAdviceLogger(RemoraAdvice remoraAdvice) {
		FileHandler handler = null;
		try {
			String path = System.getProperty(Remora.REMORA_PATH) + "/log/";
			new File(path).mkdirs();
			String pattern = path + remoraAdvice.getClass().getSimpleName() + "%u.log";
			handler = new FileHandler(pattern, LOG_FILE_SIZE, LOG_COUNT, true);
			handler.setFilter(new PassAllFilter());
			handler.setFormatter(REMORA_LOG_FORMATTER);
			handler.setLevel(Level.ALL);
		} catch (IOException e) {
			LOGGER.throwing(getClass().getName(), "ConfigureLogger", e);
		}
		Logger logger = LogManager.getLogManager().getLogger(remoraAdvice.getClass().getName());

		if (logger != null) {
			Arrays.asList(logger.getHandlers()).stream().forEach(l -> logger.removeHandler(l));
			logger.addHandler(handler);

			logger.setUseParentHandlers(false);
			logger.setLevel(Level.parse(RemoraConfig.INSTANCE.config
					.getProperty(remoraAdvice.getClass().getName() + "logLevel", Level.FINEST.getName())));

			logger.info(format("Advice logger configured, level {1}, handlers {2},  ", logger, logger.getLevel(),
					logger.getHandlers()));
			logger.severe("SEVERE is displayed");
			logger.warning("WARNING is displayed");
			logger.config("CONFIG is displayed");
			logger.info("INFO is displayed");
			logger.fine("FINE is displayed");
			logger.finer("FINER is displayed");
			logger.finer("FINEST is displayed");

		}
	}

	private class PassAllFilter implements Filter {

		@Override
		public boolean isLoggable(LogRecord record) {
			return true;
		}

	}
}
