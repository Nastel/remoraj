package com.jkoolcloud.remora.core;

import com.jkoolcloud.remora.core.utils.ReflectionUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ReflectionUtilsTest {

    public class TimeTable {

        Worker person = new Worker();
        long workingHours;
    }

    public class Worker extends Person {
        String speciality = "Programmer";

    }

    public class Person  {
        public String name = "John";
        private String health= "Good";

    }

    @Test
    public void testSingleAccessibleFieldFromDirectClass() {
        assertEquals("John",ReflectionUtils.getFieldValue("name", new Person(), String.class));
    }


    @Test
    public void testSingleAccessibleFieldFromInheritedClass()  {
        assertEquals("John",ReflectionUtils.getFieldValue("name", new Worker(), String.class));
    }

    @Test
    public void testChainedInaccessibleField()  {
        assertEquals("Good",ReflectionUtils.getFieldValue("person.health", new TimeTable(), String.class));
    }
}