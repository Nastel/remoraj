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

package com.jkoolcloud.testHarness.harnesses;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.Test;

public class MQReceiveHarnessTest {

	@Test
	public void testMQReceive() throws Exception {
		ExecutorService executorService = Executors.newSingleThreadExecutor();
		MQReceiveHarness task = new MQReceiveHarness();
		task.destination = "BankReplyQueue";
		task.setup();
		Future<HarnessResult> submit = executorService.submit(task);
		Future<HarnessResult> submit1 = executorService.submit(task);
		Future<HarnessResult> submit2 = executorService.submit(task);
		Future<HarnessResult> submit3 = executorService.submit(task);
		System.out.println(submit.get());
		System.out.println(submit1.get());
		System.out.println(submit2.get());
		System.out.println(submit3.get());
	}

}