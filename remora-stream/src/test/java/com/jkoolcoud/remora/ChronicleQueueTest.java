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

package com.jkoolcoud.remora;

import static org.junit.Assert.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Test;

import com.jkoolcloud.remora.core.EntryDefinition;
import com.jkoolcloud.tnt4j.streams.configure.ChronicleQueueProperties;
import com.jkoolcloud.tnt4j.streams.configure.StreamProperties;
import com.jkoolcloud.tnt4j.streams.inputs.ChronicleQueueStream;
import com.jkoolcloud.tnt4j.streams.inputs.StreamThread;
import com.jkoolcloud.tnt4j.streams.outputs.NullActivityOutput;
import com.jkoolcloud.tnt4j.streams.parsers.ActivityJavaObjectParser;

import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.queue.ExcerptAppender;
import net.openhft.chronicle.wire.ReadMarshallable;

/**
 * @author akausinis
 * @version 1.0
 * @created 2019-10-31 11:29
 */
public class ChronicleQueueTest {
	private EntryDefinition expect;

	@Test
	public void streamTest() throws Exception {
		// System.setProperty("log4j.configuration", "file:./config/log4j.properties");
		// ChronicleQueueStream stream = new ChronicleQueueStream();
		// stream.setProperty(StreamProperties.PROP_FILENAME,
		// "c:\\Users\\slabs\\AppData\\Local\\Temp\\chronicle-queue4202994159695497585\\");
		// stream.setProperty(ChronicleQueueProperties.PROP_MARSHALL_CLASS,
		// "com.jkoolcloud.remora.core.EntryDefinition");
		// stream.addReference(new NullActivityOutput());
		// stream.startStream();
		//
		// Object nextItem = stream.getNextItem();
		// System.out.println(nextItem);
		// assertNotNull(nextItem);
	}

	@Test
	public void streamTestWithCreateQueue() throws Exception {
		System.setProperty("log4j.configuration", "file:./config/log4j.properties");
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
		stream.setProperty(ChronicleQueueProperties.PROP_MARSHALL_CLASS, "com.jkoolcloud.remora.core.Exit");
		stream.addReference(new NullActivityOutput());
		stream.addParser(new ActivityJavaObjectParser());

		StreamThread streamThread = new StreamThread(stream);

		streamThread.start();

		expect = new EntryDefinition(ChronicleQueueTest.class);
		expect.setName("AAA");
		appender.writeDocument(expect.entry);
		Thread.sleep(300);
		expect = new EntryDefinition(ChronicleQueueTest.class);
		expect.setDuration(400L);

		appender.writeDocument(expect.exit);

		Thread.sleep(300);

		expect = new EntryDefinition(ChronicleQueueTest.class);
		expect.setException("Exeption");
		appender.writeDocument(expect.entry);
		Thread.sleep(300);
		expect = new EntryDefinition(ChronicleQueueTest.class);
		appender.writeDocument(expect.exit);
		Thread.sleep(300);
	}
}
