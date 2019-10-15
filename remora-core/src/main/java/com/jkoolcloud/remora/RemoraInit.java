package com.jkoolcloud.remora;

import java.lang.instrument.Instrumentation;
import java.util.Iterator;
import java.util.ServiceLoader;

import com.jkoolcloud.remora.advices.RemoraAdvice;

public class RemoraInit {

	// private static final Logger LOGGER = Logger.getLogger(RemoraInit.class.getName());

	public void initializeAdvices(Instrumentation inst, ClassLoader classLoader) {

		// LOGGER.info("Initializing advices: " + getClass() + " classloader: " + getClass().getClassLoader());

		ServiceLoader<RemoraAdvice> advices = ServiceLoader.load(RemoraAdvice.class, classLoader);

		Iterator<RemoraAdvice> iterator = advices.iterator();
		while (iterator.hasNext()) {
			RemoraAdvice remoraAdvice = iterator.next();
			RemoraConfig.INSTANCE.configure(remoraAdvice);
			remoraAdvice.install(inst);
			// LOGGER.info("\t Found module: " + remoraAdvice);

		}
		// LOGGER.info("Loading finished");

	}

}
