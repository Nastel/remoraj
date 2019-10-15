package com.jkoolcloud.remora;

import static com.jkoolcloud.remora.Remora.REMORA_PATH;
import static java.text.MessageFormat.format;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public enum RemoraConfig {
	INSTANCE;

	public static final String REMORA_PROPERTIES_FILE = "/config/remora.properties";

	// If anyone wonders why it's not static
	// https://stackoverflow.com/questions/49141972/nullpointerexception-in-enum-logger
	private Logger logger = Logger.getLogger(RemoraConfig.class.getName());
	public Properties config;
	public ClassLoader classLoader = null;

	RemoraConfig() {
		init();
	}

	public void configure(Object object) {

		Class<?> aClass = object.getClass();

		while (!aClass.equals(Object.class)) {
			Field[] fields = aClass.getDeclaredFields();
			for (Field field : fields) {
				if (field.isAnnotationPresent(Configurable.class)) {
					field.setAccessible(true);

					try {
						String configValue = getConfigValue(object.getClass(), field.getName());
						Object appliedValue = null;
						if (configValue != null) {
							switch (field.getType().getName()) {
							case "java.lang.String":
								appliedValue = configValue;
								break;
							case "java.util.List":
								appliedValue = getList(configValue);
								break;
							case "boolean":
								appliedValue = Boolean.parseBoolean(configValue);
								break;
							case "default":
								logger.info("Unsupported property");

							}
						}
						if (appliedValue != null) {
							logger.info(format("Setting {0} class config field \"{2}\" as {1}",
									object.getClass().getName(), appliedValue.toString(), field.getName()));
							field.set(object, appliedValue);
						}

					} catch (IllegalAccessException e) {
						e.printStackTrace();
					}
				}
			}
			aClass = aClass.getSuperclass();
		}
	}

	private List getList(String configValue) {
		if (configValue == null) {
			return null;
		}
		String[] split = configValue.split(";");
		return Arrays.asList(split).stream().map(v -> v.trim()).collect(Collectors.toList());
	}

	private String getConfigValue(Class<?> aClass, String name) {
		Class workingClass = aClass;
		String value = null;
		while (value == null && !workingClass.equals(Object.class)) {
			value = RemoraConfig.INSTANCE.config.getProperty(workingClass.getName() + "." + name);
			workingClass = workingClass.getSuperclass();
		}
		return value;
	}

	protected void init() {
		config = new Properties();

		String remoraPath = System.getProperty(REMORA_PATH);
		File file = new File(remoraPath + REMORA_PROPERTIES_FILE);
		try (FileInputStream inStream = new FileInputStream(file)) {
			config.load(inStream);
			logger.info(format("Sucessfully loaded {0} properties from configuration file", config.size()));
		} catch (IOException e) {
			logger.severe("Failed loading properties file");
			logger.info(format("Exception: {0} {1} \n {2}", "RemoraConfig", "init", e));
		}
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public @interface Configurable {
	}

}
