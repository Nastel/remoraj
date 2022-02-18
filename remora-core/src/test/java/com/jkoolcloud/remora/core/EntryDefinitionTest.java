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

package com.jkoolcloud.remora.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.jkoolcloud.remora.advices.Advice1;
import com.jkoolcloud.remora.testClasses.TestUtils;

import net.openhft.chronicle.queue.ExcerptAppender;
import net.openhft.chronicle.queue.ExcerptTailer;

public class EntryDefinitionTest {

	/**
	 * This test tests creating, writing and reading from chronicle queue output. Object pushed to the queue should be
	 * the same as dematerialised.
	 *
	 * @throws Exception
	 */
	@Test
	public void testWriteToQueue() throws Exception {
		try (TestUtils.TempQueue queue = new TestUtils.TempQueue()) {
			ExcerptAppender appender = queue.acquireAppender();
			ExcerptTailer tailer = queue.createTailer();

			EntryDefinition ed = new EntryDefinition(Advice1.class, true);
			ed.setName("AAA");
			ed.setException("Exception");
			ed.addProperty("Key", "TEST_value");

			appender.methodWriter(EntryDefinitionDescription.class).entry(ed.entry);
			// appender.writeDocument(ed);

			EntryDefinition edRead = new EntryDefinition(Advice1.class, true);
			tailer.methodReader(edRead).readOne();

			System.out.println(edRead);
			assertEquals("Name field deserialization fault", "AAA", ed.entry.name);
			assertEquals("Exception field deserialization fault", "Exception", ed.exit.exception);
			assertEquals("Properties field entry deserialization fault", "TEST_value", ed.getProperties().get("Key"));
			assertNotNull("Id field should be filled", ed.id);
		}
	}

	@Test
	public void testPropertyShift() {
		EntryDefinition entryDefinition = new EntryDefinition(Advice1.class, true);
		entryDefinition.addProperty("TEST", "1");
		entryDefinition.addProperty("TEST", "2");
		entryDefinition.addProperty("TEST", "2");
		entryDefinition.addProperty("TEST", "3");
		assertEquals("3", entryDefinition.getProperties().get("TEST"));
		assertEquals("2", entryDefinition.getProperties().get("TEST_1"));
		assertEquals("1", entryDefinition.getProperties().get("TEST_2"));
		assertEquals("1", entryDefinition.getProperties().get("TEST_2"));
		System.out.println(entryDefinition.getProperties());
	}

	@Test
	public void testPropertyShiftChrckLastPropertyValueFalse() {
		EntryDefinition entryDefinition = new EntryDefinition(Advice1.class, false);
		entryDefinition.addProperty("TEST", "1");
		entryDefinition.addProperty("TEST", "2");
		entryDefinition.addProperty("TEST", "2");
		entryDefinition.addProperty("TEST", "3");
		assertEquals("3", entryDefinition.getProperties().get("TEST"));
		assertEquals("2", entryDefinition.getProperties().get("TEST_1"));
		assertEquals("2", entryDefinition.getProperties().get("TEST_2"));
		assertEquals("1", entryDefinition.getProperties().get("TEST_3"));
		System.out.println(entryDefinition.getProperties());
	}
}