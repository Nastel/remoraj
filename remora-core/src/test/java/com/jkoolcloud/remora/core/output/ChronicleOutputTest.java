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

import static com.jkoolcloud.remora.core.EntryTest.getTestEntry;
import static com.jkoolcloud.remora.core.ExitTest.getTestExit;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import com.jkoolcloud.remora.advices.BaseTransformers;
import com.jkoolcloud.remora.advices.GeneralAdvice;
import com.jkoolcloud.remora.core.*;
import org.junit.Test;

import com.google.common.io.Files;
import com.jkoolcloud.remora.advices.Advice1;
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
			output.send(new EntryDefinition(Advice1.class, true));
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
			EntryDefinition entryDefinition = new EntryDefinition(Advice1.class, true);

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

	@Test
    public void testMultipleAppenders() {

        File tempDir = Files.createTempDir();
        //tempDir.deleteOnExit();

        ChronicleOutput output = new ChronicleOutput();
        createOutput(output, tempDir);

        EntryDefinition ed = new EntryDefinition(GeneralAdvice.class, true);
        ed.setEventType(EntryDefinition.EventType.CALL);
        ed.setCorrelator("123");
        ed.setResource("TEST", EntryDefinition.ResourceType.QUEUE);
        ed.setClazz(ChronicleOutputTest.class.getName());
        ed.setStackTrace("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        ed.setStartTime(System.currentTimeMillis());
        output.send(ed);
        ed.stop();
        ed.addProperty("a", "b");
        ed.addProperty("a", "b");
        ed.addProperty("a", "b");
        ed.addProperty("a", "b");
        ed.addProperty("a", "b");
        output.send(ed);

    }

    private void createOutput(ChronicleOutput output, File tempDir) {
        output.rollCycle = RollCycles.TEST_SECONDLY;
        output.keepQueueRolls = 2;

        System.out.println(tempDir.getAbsolutePath());
        output.queuePath = tempDir.getPath();
        output.init();
    }

}
