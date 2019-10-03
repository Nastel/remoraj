package com.jkoolcloud.remora;

import static com.jkoolcloud.remora.Remora.REMORA_PATH;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;

import org.junit.Test;

public class RemoraConfigTest {

	public class TestForSingleStringConfigurable {
		@RemoraConfig.Configurable
		String testField;
	}

	public class TestForListConfigurable {
		@RemoraConfig.Configurable
		List testField;
	}

	public class TestForListConfigurableSuperClass extends TestForListConfigurable {

	}

	@Test
	public void configTestHappyPath() throws IOException {
		Properties properties = new Properties() {
			private static final long serialVersionUID = 5832436927392520852L;

			{
				put(TestForSingleStringConfigurable.class.getName() + "." + "testField", "TEST");
			}
		};
		prepareConfigFile(properties);
		TestForSingleStringConfigurable test = new TestForSingleStringConfigurable();
		RemoraConfig.INSTANCE.configure(test);
		assertNotNull("Configurring field failed", test.testField);
	}

	@Test
	public void configTestHappyPathList() throws IOException {
		Properties properties = new Properties() {
			private static final long serialVersionUID = -2729804244126571948L;

			{
				put(TestForListConfigurable.class.getName() + "." + "testField", "TEST  ;       TEST; TEST;;");
			}
		};
		prepareConfigFile(properties);
		TestForListConfigurable test = new TestForListConfigurable();
		RemoraConfig.INSTANCE.configure(test);
		assertNotNull("Configurring field failed", test.testField);
		assertEquals("Not all of expected list values parsed", 3, test.testField.size());
	}

	@Test
	public void configTestHappyPathSuperClass() throws IOException {
		Properties properties = new Properties() {
			private static final long serialVersionUID = 7088592087312050879L;

			{
				put(TestForListConfigurableSuperClass.class.getName() + "." + "testField",
						"TEST  ;       TEST; TEST;;");
			}
		};
		prepareConfigFile(properties);
		TestForListConfigurableSuperClass test = new TestForListConfigurableSuperClass();
		RemoraConfig.INSTANCE.configure(test);
		assertNotNull("Configurring field failed", test.testField);
		assertEquals("Not all of expected list values parsed", 3, test.testField.size());
	}

	public void prepareConfigFile(Properties properties) throws IOException {
		Path remoraTempDir = Files.createTempDirectory("remoraTempDir");
		System.setProperty(REMORA_PATH, remoraTempDir.toAbsolutePath().toString());

		File file = new File(remoraTempDir.toAbsolutePath() + "/config/remora.properties");
		file.getParentFile().mkdirs();
		file.createNewFile();
		FileOutputStream out = new FileOutputStream(file);

		properties.store(out, "TEST");
	}

}