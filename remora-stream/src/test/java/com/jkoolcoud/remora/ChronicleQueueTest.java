/*
 * Copyright 2014-2019 JKOOL, LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jkoolcoud.remora;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Test;

import com.jkoolcloud.remora.core.EntryDefinition;
import com.jkoolcloud.tnt4j.streams.configure.ChronicleQueueProperties;
import com.jkoolcloud.tnt4j.streams.configure.StreamProperties;
import com.jkoolcloud.tnt4j.streams.inputs.ChronicleQueueStream;
import com.jkoolcloud.tnt4j.streams.inputs.StreamThread;
import com.jkoolcloud.tnt4j.streams.outputs.NullActivityOutput;

import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.queue.ExcerptAppender;
import net.openhft.chronicle.wire.ReadMarshallable;

/**
 * @author slb
 * @version 1.0
 * @created 2019-10-31 11:29
 */
public class ChronicleQueueTest {
	private EntryDefinition expect;

	@Test
	public void streamTest() throws Exception {
		ChronicleQueueStream stream = new ChronicleQueueStream();
		stream.setProperty(StreamProperties.PROP_FILENAME,
				"c:\\Users\\slabs\\AppData\\Local\\Temp\\chronicle-queue4202994159695497585\\");
		stream.setProperty(ChronicleQueueProperties.PROP_MARSHALL_CLASS, "com.jkoolcloud.javaam.EntryDefinition");
		stream.addReference(new NullActivityOutput());
		stream.startStream();

		Object nextItem = stream.getNextItem();
		System.out.println(nextItem);
		assertNotNull(nextItem);
	}

	@Test
	public void streamTestWithCreateQueue() throws Exception {
		ChronicleQueueStream stream = new ChronicleQueueStream() {
			@Override
			public ReadMarshallable getNextItem() throws Exception {
				ReadMarshallable item = super.getNextItem();
				System.out.println("####################################################################" + item);
				assertEquals(item, expect);

				return item;
			}
		};

		Path testQueue = Files.createTempDirectory("testQueue");

		ChronicleQueue queue = ChronicleQueue.single(testQueue.toFile().getAbsolutePath());
		ExcerptAppender appender = queue.acquireAppender();

		stream.setProperty(StreamProperties.PROP_FILENAME, testQueue.toFile().getAbsolutePath());
		stream.setProperty(ChronicleQueueProperties.PROP_MARSHALL_CLASS, "com.jkoolcloud.javaam.EntryDefinition");
		stream.addReference(new NullActivityOutput());

		StreamThread streamThread = new StreamThread(stream);
		streamThread.start();

		expect = new EntryDefinition(ChronicleQueueTest.class);
		expect.setName("AAA");
		appender.writeDocument(expect);
		Thread.sleep(300);
		expect = new EntryDefinition(ChronicleQueueTest.class);
		expect.setDuration(400L);

		appender.writeDocument(expect);

		Thread.sleep(300);

		expect = new EntryDefinition(ChronicleQueueTest.class);
		expect.setException("Exeption");
		appender.writeDocument(expect);
		Thread.sleep(300);
		expect = new EntryDefinition(ChronicleQueueTest.class);
		appender.writeDocument(expect);
		Thread.sleep(300);
	}
}
