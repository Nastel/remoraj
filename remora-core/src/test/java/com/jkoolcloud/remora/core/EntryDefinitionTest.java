package com.jkoolcloud.remora.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Test;

import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.queue.ExcerptAppender;
import net.openhft.chronicle.queue.ExcerptTailer;

public class EntryDefinitionTest {

	/**
	 * This test tests creating, writing and reading from chronicle queue output. Object pushed to the queue should be
	 * the same as dematerialised.
	 *
	 * @throws IOException
	 */

	@Test
	public void testWiteToQueue() throws IOException {
		Path tempDirectory = Files.createTempDirectory(getClass().getName());

		ChronicleQueue queue = ChronicleQueue.single(tempDirectory.toFile().getAbsolutePath());
		ExcerptAppender appender = queue.acquireAppender();
		ExcerptTailer tailer = queue.createTailer();

		EntryDefinition ed = new EntryDefinition(EntryDefinitionTest.class);
		ed.setName("AAA");
		ed.setException("Exception");
		ed.addProperty("Key", "TEST_value");

		appender.writeDocument(ed);

		EntryDefinition edRead = new EntryDefinition(EntryDefinitionTest.class);
		boolean s = tailer.readDocument(edRead);
		System.out.println(edRead);
		assertEquals("Name field deserialization fault", "AAA", ed.name);
		assertEquals("Exception field deserialization fault", "Exception", ed.exception);
		assertEquals("Properties field entry deserialization fault", "TEST_value", ed.getProperties().get("Key"));
		assertNotNull("Id field should be filled", ed.id);
	}

}