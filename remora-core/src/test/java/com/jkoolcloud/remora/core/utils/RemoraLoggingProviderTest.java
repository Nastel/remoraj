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

package com.jkoolcloud.remora.core.utils;

import java.util.Locale;

import org.junit.Test;
import org.tinylog.Level;
import org.tinylog.configuration.Configuration;
import org.tinylog.format.JavaTextMessageFormatFormatter;

public class RemoraLoggingProviderTest {

	@Test
	public void testDelaydedLogging() throws InterruptedException {
		String key = "writertest", tag = "test";
		RemoraLoggingProvider provider = new RemoraLoggingProvider();
		for (int i = 0; i < 500; i++) {
			provider.log(1, tag, Level.DEBUG, null, new JavaTextMessageFormatFormatter(Locale.getDefault()), "OOO",
					"11");
		}
		Thread.sleep(1000);
		Configuration.set(key, "console");
		// Configuration.set(key + ".file", System.getProperty(Remora.REMORA_PATH) + "/log/" + tag + ".log");
		Configuration.set(key + ".format", "{date} [{thread}] {class}.{method}()\n\t{level}: {message}");
		Configuration.set(key + ".tag", tag);
		Configuration.set(key + ".level", "TRACE");
		RemoraLoggingProvider.startLogging();
		Thread.sleep(1000);

	}

}