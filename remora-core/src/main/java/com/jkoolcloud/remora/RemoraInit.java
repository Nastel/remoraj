package com.jkoolcloud.remora;

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
	public static final String REMORA_LOG = "remora.log";

	public void initializeAdvices(Instrumentation inst, ClassLoader classLoader) {
		configureRemoraRootLogger();

		LOGGER.info("Initializing advices: " + getClass() + " classloader: " + getClass().getClassLoader());

		ServiceLoader<RemoraAdvice> advices = ServiceLoader.load(RemoraAdvice.class, classLoader);

		Iterator<RemoraAdvice> iterator = advices.iterator();
		while (iterator.hasNext()) {
			RemoraAdvice remoraAdvice = iterator.next();
			RemoraConfig.configure(remoraAdvice);
			remoraAdvice.install(inst);
			configureAdviceLogger(remoraAdvice);

			LOGGER.info("\t Found module: " + remoraAdvice);

		}
		LOGGER.info("Loading finished");

	}

	protected void configureRemoraRootLogger() {
		Logger logger = LogManager.getLogManager().getLogger("com.jkoolcloud.remora");
		try {
			logger.addHandler(new FileHandler(REMORA_LOG, LOG_FILE_SIZE, LOG_COUNT, true));
		} catch (IOException e) {
			LOGGER.throwing(getClass().getName(), "configureRemoraRootLogger", e);
		}
	}

	protected void configureAdviceLogger(RemoraAdvice remoraAdvice) {
		FileHandler handler = null;
		try {
			handler = new FileHandler(remoraAdvice.getClass().getSimpleName(), LOG_FILE_SIZE, LOG_COUNT, true);
			handler.setFilter(new Filter() {
				@Override
				public boolean isLoggable(LogRecord record) {
					return true;
				}
			});
			handler.setFormatter(new SimpleFormatter());
			handler.setLevel(Level.ALL);
		} catch (IOException e) {
			LOGGER.throwing(getClass().getName(), "cofigureLogger", e);
		}
		Logger logger = LogManager.getLogManager().getLogger(remoraAdvice.getClass().getName());

		Arrays.asList(logger.getHandlers()).stream().forEach(l -> logger.removeHandler(l));
		logger.addHandler(handler);
		logger.addHandler(new ConsoleHandler());

		logger.setUseParentHandlers(false);
		logger.setLevel(Level.parse(
				RemoraConfig.INSTANCE.config.getProperty(remoraAdvice.getClass().getName(), Level.INFO.getName())));
		logger.info("Configured");
	}
}
