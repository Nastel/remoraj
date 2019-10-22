package com.jkoolcloud.remora;

import java.lang.instrument.Instrumentation;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ServiceLoader;

import org.tinylog.Logger;
import org.tinylog.configuration.Configuration;

import com.jkoolcloud.remora.advices.RemoraAdvice;

public class RemoraInit {

	// private static final Logger LOGGER = LoggerFactory.getLogger(RemoraInit.class.getName());

	public void initializeAdvices(Instrumentation inst, ClassLoader classLoader) {

		// LOGGER.info("Initializing advices: " + getClass() + " classloader: " + getClass().getClassLoader());

		ServiceLoader<RemoraAdvice> advices = ServiceLoader.load(RemoraAdvice.class, classLoader);
		Iterator<RemoraAdvice> iterator = advices.iterator();
		ArrayList<RemoraAdvice> adviceList = new ArrayList<>();

		while (iterator.hasNext()) {
			RemoraAdvice remoraAdvice = iterator.next();
			String adviceName = remoraAdvice.getName();
			String key = "writer" + adviceName;
			Configuration.set(key, "rolling file");
			Configuration.set(key + ".file", System.getProperty(Remora.REMORA_PATH) + "/log/" + adviceName + ".log");
			Configuration.set(key + ".format", " {level}: {message}");
			Configuration.set(key + ".tag", adviceName);
			Configuration.set(key + ".level", "debug");
			RemoraConfig.INSTANCE.configure(remoraAdvice);
			adviceList.add(remoraAdvice);
			// LOGGER.info("\t Found module: " + remoraAdvice);

		}

		// need to configure logger first
		adviceList.forEach(advice -> {
			advice.install(inst);
			Logger.tag("INIT").info("Installed {}", advice.getName());
		});
		// LOGGER.info("Loading finished");

	}

}
