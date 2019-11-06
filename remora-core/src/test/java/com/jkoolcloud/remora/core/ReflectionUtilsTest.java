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
}