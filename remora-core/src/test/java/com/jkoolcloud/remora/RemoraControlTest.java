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

import java.io.IOException;

import org.junit.Test;
import org.tinylog.configuration.Configuration;

import com.jkoolcloud.remora.testClasses.TestUtils;

import net.openhft.chronicle.queue.ExcerptAppender;

public class RemoraControlTest {

	public static class TestClass {
		public static String testField = "Not Set";
	}

	@Test
	public void testBasicReadWrite() throws IOException, InterruptedException {

		Configuration.set("writerCONTROL", "console");
		Configuration.set("writerCONTROL.stream", "out");
		Configuration.set("writerCONTROL.format", "{message}");
		TestUtils.TempQueue queue = new TestUtils.TempQueue();

		RemoraControl.INSTANCE.queue = queue.getQueue();
		RemoraControl.INSTANCE.init();

		ExcerptAppender excerptAppender = queue.acquireAppender();
		RemoraControl.ControlImpl control = new RemoraControl.ControlImpl();
		excerptAppender.methodWriter(RemoraControl.Control.class)
				.control(new RemoraControl.ControlCommand(TestClass.class, "testField", "SET"));

		Thread.sleep(3000);

	}

}