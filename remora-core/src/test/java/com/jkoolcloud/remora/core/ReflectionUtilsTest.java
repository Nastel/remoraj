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

import org.junit.Test;

import com.jkoolcloud.remora.core.utils.ReflectionUtils;

public class ReflectionUtilsTest {

	public class TimeTable {

		Worker person = new Worker();
		long workingHours;
	}

	public class Worker extends Person {
		String speciality = "Programmer";

	}

	public class Person {
		public String name = "John";
		private String health = "Good";

		String getName() {
			return name;
		}

	}

	@Test
	public void testSingleAccessibleFieldFromDirectClass() {
		assertEquals("John", ReflectionUtils.getFieldValue(new Person(), String.class, "name"));
	}

	@Test
	public void testSingleAccessibleFieldFromInheritedClass() {
		assertEquals("John", ReflectionUtils.getFieldValue(new Worker(), String.class, "name"));
	}

	@Test
	public void testChainedInaccessibleField() {
		assertEquals("Good", ReflectionUtils.getFieldValue(new TimeTable(), String.class, "person.health"));
	}

	@Test
	public void invokeGetterMethodIfExist() {
		assertEquals("John", ReflectionUtils.invokeGetterMethodIfExist(new Person(), "getName"));
	}
}