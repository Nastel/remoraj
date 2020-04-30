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

package com.jkoolcloud.remora.core.output;

import static junit.framework.TestCase.fail;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.text.MessageFormat;

import org.junit.BeforeClass;
import org.junit.Test;
import org.tinylog.Logger;
import org.tinylog.TaggedLogger;
import org.tinylog.configuration.Configuration;

public class ScheduledQueueErrorReporterTest {

	public static final Integer DELAY = 2;
	private static TaggedLogger logger;
	private long lastWrite = 0;
	private boolean fail;
	private final PrintStream originalOut = System.out;
	private final ByteArrayOutputStream outContent = new ByteArrayOutputStream() {
		@Override
		public synchronized void write(byte[] b, int off, int len) {
			long now = System.currentTimeMillis();

			if (lastWrite != 0 && (now - lastWrite) < ((DELAY * 1000) - 100)
					&& (now - lastWrite) > ((DELAY * 1000) + 100)) {
				fail = true;

				originalOut.println(MessageFormat.format("About to fail: time between logs {0}", now - lastWrite));
			}
			lastWrite = now;
			super.write(b, off, len);
		}
	};

	@BeforeClass
	public static void configureLogger() {
		Configuration.set("writerTEST", "console");
		Configuration.set("writerTEST.stream", "out");
		Configuration.set("writerTEST.format", "{message}");

		logger = Logger.tag("TEST");
	}

	@Test
	public void testErrorReporter() throws InterruptedException {

		System.setOut(new PrintStream(outContent) {
			@Override
			public void print(String s) {
				originalOut.print(s);
				super.print(s);
			}
		});

		ScheduledQueueErrorReporter scheduledQueueErrorReporterTest = new ScheduledQueueErrorReporter(logger, DELAY);

		for (int i = 0; i <= 50; i++) {
			ScheduledQueueErrorReporter.chronicleQueueFailCount.incrementAndGet();
			Thread.sleep(100);
		}
		for (int i = 0; i <= 50; i++) {
			ScheduledQueueErrorReporter.intermediateQueueFailCount.incrementAndGet();
			Thread.sleep(100);
		}
		if (fail) {
			fail();
		}

	}

}