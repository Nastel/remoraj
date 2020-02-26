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

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.Arrays;

import org.junit.Test;

import com.google.common.io.Files;
import com.jkoolcloud.remora.core.EntryDefinition;

import net.openhft.chronicle.queue.RollCycles;

public class ChronicleOutputTest {

	@Test
	public void testRolling() throws InterruptedException {
		ChronicleOutput output = new ChronicleOutput();
		output.rollCycle = RollCycles.TEST_SECONDLY;
		output.keepQueueRolls = 2;
		File tempDir = Files.createTempDir();
		tempDir.deleteOnExit();
		System.out.println(tempDir.getAbsolutePath());
		output.queuePath = tempDir.getPath();
		output.init();

		for (int i = 0; i <= 50; i++) {
			output.send(new EntryDefinition(ChronicleOutputTest.class));
			Thread.sleep(100);
		}
		assertEquals(tempDir.list().length, output.keepQueueRolls.intValue() + 1 + 1); // +1 = metadata; +1 = current
		output.shutdown();
		assertEquals(tempDir.list().length, output.keepQueueRolls.intValue() + 1); // +1 = current after shutdown
																					// queue
		Arrays.asList(tempDir.listFiles()).forEach(file -> {
			while (!file.delete()) {
				// make sure it deletes
			}
		});
		tempDir.delete();
	}

}