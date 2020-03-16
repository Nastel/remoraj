/*
 *
 * Copyright (c) 2019-2020 NasTel Technologies, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of NasTel
 * Technologies, Inc. ("Confidential Information").  You shall not disclose
 * such Confidential Information and shall use it only in accordance with
 * the terms of the license agreement you entered into with NasTel
 * Technologies.
 *
 * NASTEL MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF
 * THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE, OR NON-INFRINGEMENT. NASTEL SHALL NOT BE LIABLE FOR ANY DAMAGES
 * SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
 * THIS SOFTWARE OR ITS DERIVATIVES.
 *
 * CopyrightVersion 1.0
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
		ArrayList<RemoraAdvice> adviceList = new ArrayList<>();
		HashMap<RemoraAdvice, Exception> failedList = new HashMap<>();

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
				RemoraConfig.INSTANCE.configure(remoraAdvice);
				adviceList.add(remoraAdvice);
			} catch (Exception e) {
				failedList.put(remoraAdvice, e);
			}

			// LOGGER.info("\t Found module: " + remoraAdvice);

		}

		// need to configure logger first
		adviceList.forEach(advice -> {
			advice.install(inst);
			Logger.tag("INIT").info("Installed {}", advice.getName());
		});

		RemoraControl.INSTANCE.init();
		RemoraControl.INSTANCE.report(adviceList);

		failedList.forEach((advice, exc) -> {
			Logger.tag("INIT").info("Failed configuring: ", advice.getName());
			Logger.tag("INIT").info(exc);
		});
		// LOGGER.info("Loading finished");

	}

}
