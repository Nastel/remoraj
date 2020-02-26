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

package com.jkoolcloud.testHarness;

import java.io.PrintWriter;
import java.util.ArrayDeque;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.jkoolcloud.testHarness.harnesses.MQReceiveHarness;
import com.jkoolcloud.testHarness.harnesses.PeriodicRunnableHarness;
import com.jkoolcloud.testHarness.harnesses.SoutHarness;

public class SetupTest {

	private ArrayDeque<Integer> results = new ArrayDeque<>();

	@Test
	public void teshSchedulledExcetur() throws InterruptedException {
		MyCallable myCallable = new MyCallable();
		ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
		scheduledExecutorService.scheduleAtFixedRate(new PeriodicRunnableHarness(new SoutHarness()), 0, 1000,
				TimeUnit.MILLISECONDS);
		Thread.sleep(5000);
		System.out.println(results);
	}

	class MyCallable implements Callable<Integer> {

		@Override
		public Integer call() throws Exception {
			System.out.println("Run");
			return 1;
		}
	}

	@Test
	public void testPrintConfigurables() {
		PrintWriter printWriter = new PrintWriter(System.out);
		Setup.printConfigurables(printWriter, MQReceiveHarness.class);
		printWriter.flush();
	}

	@Test
	public void testMe() throws NoSuchFieldException, IllegalAccessException {
		Entity entity = new Entity();
		Entity2 entity2 = (Entity2) entity;
		System.out.println(entity.getClass().getField("aa").get(entity));
		System.out.println(entity2.getClass().getField("aa").get(entity2));

	}

	public class Entity {
		public String aa = "AA";

	}

	public class Entity2 extends Entity {
		public String aa = "BB";
	}

}