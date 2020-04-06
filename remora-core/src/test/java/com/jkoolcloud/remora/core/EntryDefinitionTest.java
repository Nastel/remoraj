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

package com.jkoolcloud.remora.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

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
	public void testWiteToQueue() throws Exception {
		try (TestUtils.TempQueue queue = new TestUtils.TempQueue()) {

			ExcerptAppender appender = queue.acquireAppender();
			ExcerptTailer tailer = queue.createTailer();

			EntryDefinition ed = new EntryDefinition(EntryDefinitionTest.class, true);
			ed.setName("AAA");
			ed.setException("Exception");
			ed.addProperty("Key", "TEST_value");

			appender.methodWriter(EntryDefinitionDescription.class).entry(ed.entry);
			// appender.writeDocument(ed);

			EntryDefinition edRead = new EntryDefinition(EntryDefinitionTest.class, true);
			boolean s = tailer.methodReader(edRead).readOne();

			System.out.println(edRead);
			assertEquals("Name field deserialization fault", "AAA", ed.entry.name);
			assertEquals("Exception field deserialization fault", "Exception", ed.exit.exception);
			assertEquals("Properties field entry deserialization fault", "TEST_value", ed.getProperties().get("Key"));
			assertNotNull("Id field should be filled", ed.id);
		}
	}

	@Test
	public void testPropertyShift() {
		EntryDefinition entryDefinition = new EntryDefinition(EntryDefinitionTest.class, true);
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
		EntryDefinition entryDefinition = new EntryDefinition(EntryDefinitionTest.class, false);
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