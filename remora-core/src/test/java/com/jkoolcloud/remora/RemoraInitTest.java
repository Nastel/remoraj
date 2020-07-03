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
		String key = "writer" + "NO";
		String adviceName = "NO";

		org.tinylog.Logger.tag("INIT").info("HI");
		Configuration.set(key, "rolling file");
		Configuration.set(key + ".file", adviceName + ".log");
		Configuration.set(key + ".format", " {level}: {message}");
		Configuration.set(key + ".tag", adviceName);
		Configuration.set(key + ".level", "debug");
		org.tinylog.Logger.tag("NO").info("HI");
		org.tinylog.Logger.tag("ED").info("ED");

		org.tinylog.Logger.info("HI");
	}

}
