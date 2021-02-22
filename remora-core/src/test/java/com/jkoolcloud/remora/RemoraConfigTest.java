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

package com.jkoolcloud.remora;

import static com.jkoolcloud.remora.Remora.REMORA_PATH;
import static org.junit.Assert.*;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import com.jkoolcloud.remora.core.EntryDefinition;
import com.jkoolcloud.remora.core.output.AgentOutput;
import com.jkoolcloud.remora.core.output.NullOutput;
import com.jkoolcloud.remora.filters.AdviceFilter;
import com.jkoolcloud.remora.filters.FilterManager;

public class RemoraConfigTest {

	private Path remoraTempDir;

	public enum TestEnum {
		ONE, TWO, THREE
	}

	@Before
	public void sllep() throws InterruptedException {
		Thread.sleep(1000);
	}

	public static class TestForSingleStringConfigrable {
		@RemoraConfig.Configurable
		String testField;
	}

	public static class TestForNumbersConfigrable {
		@RemoraConfig.Configurable
		Integer testField;
		@RemoraConfig.Configurable
		int testField2;
		@RemoraConfig.Configurable
		long testField3;
		@RemoraConfig.Configurable
		Long testField4;
	}

	public static class TestForListConfigrable {
		@RemoraConfig.Configurable
		List<?> testField;
		@RemoraConfig.Configurable
		static boolean logging;
	}

	public static class TestForBooleanConfigrable {
		@RemoraConfig.Configurable
		boolean testField;
	}

	public static class TestForEnumConfigurable {
		@RemoraConfig.Configurable
		TestEnum testField = TestEnum.THREE;
	}

	public static class TestForFilters {
		@RemoraConfig.Configurable
		List<AdviceFilter> filters = new ArrayList<>();
	}

	public static class TestForClassInstances {
		@RemoraConfig.Configurable
		AgentOutput<EntryDefinition> output = null;
	}

	public static class TestForListConfigrableSuperClass extends TestForListConfigrable {

	}

	@Test
	public void configTestHappyPath() throws Exception {
		Properties properties = new Properties() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			{
				put(TestForSingleStringConfigrable.class.getName() + "." + "testField", "TEST");
			}
		};
		prepareConfigFile(properties);
		TestForSingleStringConfigrable test = new TestForSingleStringConfigrable();
		RemoraConfig.INSTANCE.init(); // you need to initialise repeatidly 'cause multiple tests will fail
		RemoraConfig.configure(test);
		cleanup();
		assertNotNull("Configuring field failed", test.testField);
	}

	@Test
	public void configTestHappyPathNumbers() throws Exception {
		Properties properties = new Properties() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			{
				put(TestForNumbersConfigrable.class.getName() + "." + "testField", "1");
				put(TestForNumbersConfigrable.class.getName() + "." + "testField2", "2");
				put(TestForNumbersConfigrable.class.getName() + "." + "testField3", "3");
				put(TestForNumbersConfigrable.class.getName() + "." + "testField4", "4");
			}
		};
		prepareConfigFile(properties);
		TestForNumbersConfigrable test = new TestForNumbersConfigrable();
		RemoraConfig.INSTANCE.init(); // you need to initialize repeatedly 'cause multiple tests will fail
		RemoraConfig.configure(test);
		cleanup();
		assertEquals("Configuring field Integer failed", new Integer(1), test.testField);
		assertEquals("Configuring field int failed", 2, test.testField2);
		assertEquals("Configuring field Long failed", 3L, test.testField3);
		assertEquals("Configuring field long failed", new Long(4L), test.testField4);
	}

	@Test
	public void configTestBooleanHappyPath() throws Exception {
		Properties properties = new Properties() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			{
				put(TestForBooleanConfigrable.class.getName() + "." + "testField", "true");
			}
		};
		prepareConfigFile(properties);
		TestForBooleanConfigrable test = new TestForBooleanConfigrable();
		RemoraConfig.INSTANCE.init(); // you need to initialise repeatidly 'cause multiple tests will fail
		RemoraConfig.configure(test);
		cleanup();
		assertTrue("Configuring field failed", test.testField);
	}

	@Test
	public void configTestHappyPathList() throws Exception {
		Properties properties = new Properties() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			{
				put(TestForListConfigrable.class.getName() + "." + "testField", "TEST  ;       TEST; TEST;;");
			}
		};
		prepareConfigFile(properties);
		TestForListConfigrable test = new TestForListConfigrable();
		RemoraConfig.INSTANCE.init(); // you need to initialise repeatidly 'cause multiple tests will fail
		RemoraConfig.configure(test);
		cleanup();
		assertNotNull("Configurring field failed", test.testField);
		assertEquals("Not all of expected list values parsed", 3, test.testField.size());
	}

	@Test
	public void configTestHappyPathEnum() throws Exception {
		Properties properties = new Properties() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			{
				put(TestForEnumConfigurable.class.getName() + "." + "testField", "ONE");
			}
		};
		prepareConfigFile(properties);
		TestForEnumConfigurable test = new TestForEnumConfigurable();
		RemoraConfig.INSTANCE.init(); // you need to initialise repeatidly 'cause multiple tests will fail
		RemoraConfig.configure(test);
		cleanup();
		assertEquals("Configurring field failed", test.testField, TestEnum.ONE);
	}

	@Test
	public void configTestHappyPathSuperClass() throws Exception {
		Properties properties = new Properties() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			{
				put(TestForListConfigrable.class.getName() + "." + "testField", "TEST  ;       TEST; TEST;;");
				put(TestForListConfigrableSuperClass.class.getName() + "." + "logging", "false");
			}
		};
		prepareConfigFile(properties);
		TestForListConfigrableSuperClass test = new TestForListConfigrableSuperClass();
		RemoraConfig.INSTANCE.init(); // you need to initialise repeatidly 'cause multiple tests will fail
		RemoraConfig.configure(test);
		cleanup();
		assertNotNull("Configurring field failed", test.testField);
		assertNotNull("Logging field failed", TestForListConfigrable.logging);
		assertEquals("Not all of expected list values parsed", 3, test.testField.size());
	}

	public void prepareConfigFile(Properties properties) throws Exception {
		remoraTempDir = Files.createTempDirectory("remoraTempDir");
		System.setProperty(REMORA_PATH, remoraTempDir.toAbsolutePath().toString());

		File file = new File(remoraTempDir.toAbsolutePath() + "/config/remora.properties");
		file.getParentFile().mkdirs();
		file.createNewFile();
		FileOutputStream out = new FileOutputStream(file);

		properties.store(out, "TEST");
		out.close();

	}

	public void cleanup() {
		try {
			FileUtils.deleteDirectory(remoraTempDir.toFile());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testConfigFilters() throws Exception {
		Properties properties = new Properties() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			{
				put("filter.myDefinedFilter.type", "com.jkoolcloud.remora.filters.ClassNameFilter");
				put("filter.myDefinedFilter.mode", "EXCLUDE");
				put("filter.myDefinedFilter.classes", "java.net.SocketInputStream");
				put(TestForFilters.class.getName() + ".filters", "myDefinedFilter");
			}
		};
		prepareConfigFile(properties);
		RemoraConfig.INSTANCE.init(); // you need to initialise repeatidly 'cause multiple tests will fail
		List<AdviceFilter> filterList = FilterManager.INSTANCE.get(Collections.singletonList("myDefinedFilter"));
		assertEquals(1, filterList.size());
		AdviceFilter myDefinedFilter = filterList.get(0);
		assertNotNull(myDefinedFilter);
		assertEquals(myDefinedFilter.getMode(), AdviceFilter.Mode.EXCLUDE);

		TestForFilters testObject = new TestForFilters();
		RemoraConfig.configure(testObject);

		assertEquals(1, testObject.filters.size());
		assertEquals(AdviceFilter.Mode.EXCLUDE, testObject.filters.get(0).getMode());

	}

	@Test
	public void testConfigForClassInstances() throws Exception {
		Properties properties = new Properties() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			{
				put(TestForClassInstances.class.getName() + ".output", NullOutput.class.getName());
			}
		};
		prepareConfigFile(properties);
		RemoraConfig.INSTANCE.init(); // you need to initialise repeatidly 'cause multiple tests will fail
		TestForClassInstances test = new TestForClassInstances();
		RemoraConfig.configure(test);
		assertTrue(test.output instanceof NullOutput);

	}

}
