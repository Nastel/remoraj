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

import static junit.framework.TestCase.assertEquals;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.mockito.internal.util.StringUtil;
import org.tinylog.configuration.Configuration;

import com.jkoolcloud.remora.advices.RemoraAdvice;
import com.jkoolcloud.remora.testClasses.TestUtils;

import net.openhft.chronicle.queue.ExcerptAppender;

public class RemoraControlTest {

	public static class TestClass implements RemoraAdvice {

		@RemoraConfig.Configurable
		public static String testField = "Not Set";
		@RemoraConfig.Configurable
		public static String testField2 = "Not Set";

		// NotConsigurable
		public static String testField3 = "Not Set";

		@Override
		public void install(Instrumentation inst) {
		}

		@Override
		public String getName() {
			return null;
		}
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

		assertEquals(TestClass.testField, "SET");
		queue.close();

	}

	@Test
	public void testReportAdvices() throws IOException, InterruptedException {

		Configuration.set("writerCONTROL", "console");
		Configuration.set("writerCONTROL.stream", "out");
		Configuration.set("writerCONTROL.format", "{message}");
		TestUtils.TempQueue queue = new TestUtils.TempQueue();

		RemoraControl.INSTANCE.queue = queue.getQueue();
		RemoraControl.INSTANCE.report(Collections.singletonList(new TestClass()));

		System.out.println(queue.getQueue().dump());

		queue.close();

	}

	@Test
	public void testgetConfigurableFields() throws IOException, InterruptedException {
		List<String> configurableFields = RemoraControl.INSTANCE.getConfigurableFields(new TestClass());
		System.out.println(StringUtil.join(configurableFields, " "));
		assertEquals(2, configurableFields.size());
	}

}