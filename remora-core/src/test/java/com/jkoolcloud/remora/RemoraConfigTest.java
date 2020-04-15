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

package com.jkoolcloud.remora;

import static com.jkoolcloud.remora.Remora.REMORA_PATH;
import static org.junit.Assert.*;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

public class RemoraConfigTest {

	private Path remoraTempDir;

	public enum TestEnum {
		ONE, TWO, THREE
	}

	public static class TestForSingleStringConfigrable {
		@RemoraConfig.Configurable
		String testField;
	}

	public static class TestForListConfigrable {
		@RemoraConfig.Configurable
		List testField;
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

	public static class TestForListConfigrableSuperClass extends TestForListConfigrable {

	}

	@Test
	public void configTestHappyPath() throws Exception {
		Properties properties = new Properties() {
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
	public void configTestBooleanHappyPath() throws Exception {
		Properties properties = new Properties() {
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

}