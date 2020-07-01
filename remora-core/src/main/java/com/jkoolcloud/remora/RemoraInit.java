/*
 * Copyright 2019-2020 NASTEL TECHNOLOGIES, INC.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.jkoolcloud.remora;

import java.lang.instrument.Instrumentation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ServiceLoader;

import org.tinylog.Logger;
import org.tinylog.configuration.Configuration;

import com.jkoolcloud.remora.advices.RemoraAdvice;

public class RemoraInit {

	// private static final Logger LOGGER = LoggerFactory.getLogger(RemoraInit.class.getName());

	public static void initializeAdvices(Instrumentation inst, ClassLoader classLoader) {

		// LOGGER.info("Initializing advices: " + getClass() + " classloader: " + getClass().getClassLoader());

		ServiceLoader<RemoraAdvice> advices = ServiceLoader.load(RemoraAdvice.class, classLoader);
		Iterator<RemoraAdvice> iterator = advices.iterator();
		ArrayList<RemoraAdvice> adviceList = new ArrayList<>(50);
		HashMap<RemoraAdvice, Exception> failedList = new HashMap<>(50);

		while (iterator.hasNext()) {
			RemoraAdvice remoraAdvice = iterator.next();
			String adviceName = remoraAdvice.getName();
			String key = "writer" + adviceName;
			Configuration.set(key, "rolling file");
			Configuration.set(key + ".file", System.getProperty(Remora.REMORA_PATH) + "/log/" + adviceName + ".log");
			Configuration.set(key + ".format", " {level}: {message}");
			Configuration.set(key + ".tag", adviceName);
			Configuration.set(key + ".level", "debug");
			try {
				RemoraConfig.configure(remoraAdvice);
				adviceList.add(remoraAdvice);
			} catch (Exception e) {
				failedList.put(remoraAdvice, e);
			}

			// LOGGER.info("\t Found module: " + remoraAdvice);

		}
		try {
			RemoraConfig.configure(AdviceRegistry.INSTANCE);
		} catch (IllegalAccessException e) {
			Logger.tag("INIT").info("AdviceRegistry Config failed");
		}
		AdviceRegistry.INSTANCE.report(adviceList);
		// need to configure logger first
		adviceList.forEach(advice -> {
			advice.install(inst);
			Logger.tag("INIT").info("Installed {}", advice.getName());
		});

		failedList.forEach((advice, exc) -> {
			Logger.tag("INIT").info("Failed configuring: ", advice.getName());
			Logger.tag("INIT").info(exc);
		});
		// LOGGER.info("Loading finished");

	}

}
