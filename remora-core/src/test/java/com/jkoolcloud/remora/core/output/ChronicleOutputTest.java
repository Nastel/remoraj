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

import static com.jkoolcloud.remora.core.EntryTest.getTestEntry;
import static com.jkoolcloud.remora.core.ExitTest.getTestExit;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.junit.Test;

import com.google.common.io.Files;
import com.jkoolcloud.remora.core.Entry;
import com.jkoolcloud.remora.core.EntryDefinition;
import com.jkoolcloud.remora.core.EntryDefinitionDescription;
import com.jkoolcloud.remora.core.Exit;
import com.jkoolcloud.remora.testClasses.TestUtils;

import net.openhft.chronicle.queue.ExcerptAppender;
import net.openhft.chronicle.queue.ExcerptTailer;
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
			output.send(new EntryDefinition(ChronicleOutputTest.class, true));
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

	@Test
	public void testCompatibleWrite() throws IOException {
		try (TestUtils.TempQueue queue = new TestUtils.TempQueue()) {
			ExcerptAppender excerptAppender = queue.acquireAppender();
			ExcerptTailer tailer = queue.createTailer();

			Entry entry = getTestEntry();
			Exit exit = getTestExit();
			EntryDefinition entryDefinition = new EntryDefinition(getClass(), true);

			excerptAppender.methodWriter(EntryDefinitionDescription.class).entry(entry);
			entry.write(excerptAppender);

			tailer.methodReader(entryDefinition).readOne();

			assertEquals(entry, entryDefinition.entry);
			tailer.methodReader(entryDefinition).readOne();
			assertEquals(entry, entryDefinition.entry);

			excerptAppender.methodWriter(EntryDefinitionDescription.class).exit(exit);
			exit.write(excerptAppender);
			tailer.methodReader(entryDefinition).readOne();
			assertEquals(exit, entryDefinition.exit);
			tailer.methodReader(entryDefinition).readOne();
			assertEquals(exit, entryDefinition.exit);

			System.out.println(queue.getQueue().dump());

		}
	}

}