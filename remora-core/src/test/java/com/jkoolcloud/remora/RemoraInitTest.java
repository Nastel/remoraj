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

import org.junit.Test;
import org.tinylog.Logger;
import org.tinylog.TaggedLogger;
import org.tinylog.configuration.Configuration;

public class RemoraInitTest {

	@Test
	public void configureLogger() {
		System.setProperty(Remora.REMORA_PATH, ".");
		@SuppressWarnings("unused")
		RemoraInit init = new RemoraInit();
		// Remora.configureRemoraRootLogger(".");
		// init.configureAdviceLogger(new Advice1());
		// init.configureAdviceLogger(new Advice2());
	}

	public static void main(String[] args) {
		String key2 = "test2", key = "test", adviceName = "test";
		ThreadLocal<String> string = new ThreadLocal<>();
		string.set("SSSS");
		// Configure
		Configuration.set(key, "remora");
		// Configuration.set(key + ".file", System.getProperty(Remora.REMORA_PATH) + "/log/" + adviceName + ".log");
		Configuration.set(key + ".format", "{date} [{thread}] {class}.{method}()\n\t{level}: {message}");
		Configuration.set(key + ".tag", adviceName);
		Configuration.set(key + ".level", "debug");
		TaggedLogger logger = Logger.tag("test");
		doTestLogging(logger);

		System.out.println("RECONFIGURE");
		// Reconfigure
		Configuration.set(key, "remora");
		// Configuration.set(key + ".file", System.getProperty(Remora.REMORA_PATH) + "/log/" + adviceName + ".log");
		Configuration.set(key + ".format", "{date} [{thread}] {class}.{method}()\n\t{level}: {message}");
		Configuration.set(key + ".tag", adviceName);
		Configuration.set(key + ".level", "error");
		logger = Logger.tag("test");

		doTestLogging(logger);

	}

	private static void doTestLogging(TaggedLogger logger) {
		logger.info("INFO");
		logger.warn("WARN");
		logger.error("ERROR");
		logger.debug("DEBUG");
		logger.trace("TRACE");
		logger.error(new Exception("Exception"));

	}

	@Test
	public void testGetEnv() {
		Remora.logger = Logger.tag(Remora.MAIN_REMORA_LOGGER);
		Remora.logRunEnv();
	}
}
