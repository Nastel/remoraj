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