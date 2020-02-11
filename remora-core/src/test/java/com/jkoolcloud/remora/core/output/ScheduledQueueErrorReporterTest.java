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
			scheduledQueueErrorReporterTest.chronicleQueueFailCount.incrementAndGet();
			Thread.sleep(100);
		}
		for (int i = 0; i <= 50; i++) {
			scheduledQueueErrorReporterTest.intermediateQueueFailCount.incrementAndGet();
			Thread.sleep(100);
		}
		if (fail) {
			fail();
		}

	}

}