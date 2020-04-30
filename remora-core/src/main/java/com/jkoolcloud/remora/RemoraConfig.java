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
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;

public enum RemoraConfig {
	INSTANCE;

	public static final String REMORA_PROPERTIES_FILE = "/config/remora.properties";

	// If anyone wonders why it's not static
	// https://stackoverflow.com/questions/49141972/nullpointerexception-in-enum-logger
	// private TaggedLogger logger = Logger.tag("INIT");
	public Properties config;
	public ClassLoader classLoader = null;

	RemoraConfig() {
		init();
	}

	public static void configure(Object object) throws IllegalAccessException {
		Class<?> aClass = object.getClass();

		while (!aClass.equals(Object.class)) {
			Field[] fields = aClass.getDeclaredFields();
			for (Field field : fields) {
				if (field.isAnnotationPresent(Configurable.class)) {
					field.setAccessible(true);

					String configValue = getConfigValue(object.getClass(), field.getName());
					Object appliedValue = getAppliedValue(field, configValue);
					if (appliedValue != null) {
						// logger.info(format("Setting {} class config field \"{}\" as {}",
						// object.getClass().getName(), appliedValue.toString(), field.getName()));
						field.set(object, appliedValue);
					}

				}
			}
			aClass = aClass.getSuperclass();
		}
	}

	@Nullable
	@SuppressWarnings("unchecked")
	public static Object getAppliedValue(Field field, String configValue) {
		Object appliedValue = null;
		if (configValue != null) {
			if (field.getType().isEnum()) {
				appliedValue = Enum.valueOf((Class<Enum>) field.getType(), configValue);
			} else {
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
					// logger.info("Unsupported property");

				}
			}
		}
		return appliedValue;
	}

	private static List<?> getList(String configValue) {
		if (configValue == null) {
			return null;
		}
		String[] split = configValue.split(";");
		return Arrays.stream(split).map(v -> v.trim()).collect(Collectors.toList());
	}

	private static String getConfigValue(Class<?> aClass, String name) {
		Class<?> workingClass = aClass;
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
			// logger.info("Sucessfully loaded {} properties from configuration file", config.size()));
		} catch (IOException e) {
			// logger.error("Failed loading properties file");
			// logger.info("Exception: {} {} \n {}", "RemoraConfig", "init", e));
		}
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public @interface Configurable {
		boolean configurableOnce() default false;
	}

}
