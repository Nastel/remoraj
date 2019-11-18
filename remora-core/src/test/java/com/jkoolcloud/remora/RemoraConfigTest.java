package com.jkoolcloud.remora;

import static com.jkoolcloud.remora.Remora.REMORA_PATH;
import static org.junit.Assert.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

public class RemoraConfigTest {

	private Path remoraTempDir;

	public static class TestForSingleStringConfigrable {
		@RemoraConfig.Configurable
		String testField;
	}

	public static class TestForListConfigrable {
		@RemoraConfig.Configurable
		List testField;
	}

	public static class TestForBooleanConfigrable {
		@RemoraConfig.Configurable
		boolean testField;
	}

	public static class TestForListConfigrableSuperClass extends TestForListConfigrable {

	}

	@Test
	public void configTestHappyPath() throws IOException {
		Properties properties = new Properties() {
			{
				put(TestForSingleStringConfigrable.class.getName() + "." + "testField", "TEST");
			}
		};
		prepareConfigFile(properties);
		TestForSingleStringConfigrable test = new TestForSingleStringConfigrable();
		RemoraConfig.INSTANCE.init(); // you need to initialise repeatidly 'cause multiple tests will fail
		RemoraConfig.INSTANCE.configure(test);
		cleanup();
		assertNotNull("Configuring field failed", test.testField);
	}

	@Test
	public void configTestBooleanHappyPath() throws IOException {
		Properties properties = new Properties() {
			{
				put(TestForBooleanConfigrable.class.getName() + "." + "testField", "true");
			}
		};
		prepareConfigFile(properties);
		TestForBooleanConfigrable test = new TestForBooleanConfigrable();
		RemoraConfig.INSTANCE.init(); // you need to initialise repeatidly 'cause multiple tests will fail
		RemoraConfig.INSTANCE.configure(test);
		cleanup();
		assertTrue("Configuring field failed", test.testField);
	}

	@Test
	public void configTestHappyPathList() throws IOException {
		Properties properties = new Properties() {
			{
				put(TestForListConfigrable.class.getName() + "." + "testField", "TEST  ;       TEST; TEST;;");
			}
		};
		prepareConfigFile(properties);
		TestForListConfigrable test = new TestForListConfigrable();
		RemoraConfig.INSTANCE.init(); // you need to initialise repeatidly 'cause multiple tests will fail
		RemoraConfig.INSTANCE.configure(test);
		cleanup();
		assertNotNull("Configurring field failed", test.testField);
		assertEquals("Not all of expected list values parsed", 3, test.testField.size());
	}

	@Test
	public void configTestHappyPathSuperClass() throws IOException {
		Properties properties = new Properties() {
			{
				put(TestForListConfigrable.class.getName() + "." + "testField", "TEST  ;       TEST; TEST;;");
			}
		};
		prepareConfigFile(properties);
		TestForListConfigrableSuperClass test = new TestForListConfigrableSuperClass();
		RemoraConfig.INSTANCE.init(); // you need to initialise repeatidly 'cause multiple tests will fail
		RemoraConfig.INSTANCE.configure(test);
		cleanup();
		assertNotNull("Configurring field failed", test.testField);
		assertEquals("Not all of expected list values parsed", 3, test.testField.size());
	}

	public void prepareConfigFile(Properties properties) throws IOException {
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
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}