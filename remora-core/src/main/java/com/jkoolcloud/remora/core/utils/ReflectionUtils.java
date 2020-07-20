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

package com.jkoolcloud.remora.core.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;

import com.jkoolcloud.remora.RemoraConfig;

public class ReflectionUtils {
	@SuppressWarnings("unchecked")
	public static <T> T getFieldValue(Object object, Class<T> expectedReturn, String... paths) {
		for (String path : paths) {
			String[] levels = path.split("\\.");

			Object workingObject = object;
			for (String level : levels) {
				try {
					Field field = _getField(level, workingObject);
					Object o = field.get(workingObject);
					workingObject = o;
				} catch (NoSuchFieldException e) {
					break;
				} catch (IllegalAccessException e) {
					// we set it accessible
				}
			}
			if (expectedReturn.isInstance(workingObject)) {
				return (T) workingObject;
			} else {
				continue;
			}
		}
		throw new IllegalArgumentException("Type mismatch");

	}

	public static Field _getField(String fieldName, Object object) throws NoSuchFieldException {
		Field declaredField = null;
		Class<?> aClass = object.getClass();
		do {
			try {
				declaredField = aClass.getDeclaredField(fieldName);
				break;
			} catch (NoSuchFieldException e) {
				aClass = aClass.getSuperclass();
				continue;
			}
		} while (!aClass.equals(Object.class));

		if (declaredField == null) {
			throw new NoSuchFieldException();
		}
		if (!declaredField.isAccessible()) {
			declaredField.setAccessible(true);
		}
		return declaredField;
	}

	public static Object invokeGetterMethodIfExist(Object object, String name) {

		Object ret = null;
		try {

			Method method = null;
			try {
				method = object.getClass().getDeclaredMethod(name);
			} catch (NoSuchMethodException e) {

				try {
					method = object.getClass().getMethod(name);
				} catch (NoSuchMethodException e2) {
				}
			}
			if (method != null) {
				method.setAccessible(true);
				ret = method.invoke(object);
			}
		} catch (Exception e) {
		}
		return ret;
	}

	@NotNull
	public static ArrayList<Field> geAllDeclaredtFields(Class<?> aClass) {
		ArrayList<Field> declaredFields = new ArrayList<>();
		for (Class<?> c = aClass; c != null; c = c.getSuperclass()) {
			declaredFields.addAll(Arrays.asList(c.getDeclaredFields()));
		}
		return declaredFields;
	}

	public static Field getFieldFromAllDeclaredFields(Class<?> aClass, String fieldName) {
		for (Class<?> c = aClass; c != null; c = c.getSuperclass()) {
			try {
				return c.getDeclaredField(fieldName);
			} catch (NoSuchFieldException e) {
				continue;
			}

		}
		return null;
	}

	public static List<String> getConfigurableFields(Object advice) {

		Class<?> aClass = advice.getClass();
		ArrayList<Field> declaredFields = geAllDeclaredtFields(aClass);

		return declaredFields.stream()
				.filter(field -> field.isAnnotationPresent(RemoraConfig.Configurable.class)
						&& !field.getAnnotation(RemoraConfig.Configurable.class).configurableOnce())
				.map(field -> field.getName()).collect(Collectors.toList());
	}

	public static Map<String, Object> mapToCurrentValues(Object advice, List<String> availableConfigurationFields)
			throws Exception {
		Exception[] exception = new Exception[1];
		Map<String, Object> collect = availableConfigurationFields.stream()
				.collect(Collectors.toMap(fName -> fName, fName -> {

					try {
						Field declaredField = getFieldFromAllDeclaredFields(advice.getClass(), fName);
						declaredField.setAccessible(true);
						Object value = declaredField.get(advice);

						return value == null ? "null" : value;
					} catch (Exception e) {
						exception[0] = e;
					}
					return "N/A";
				}));
		if (exception[0] != null) {
			throw exception[0];
		}
		return collect;
	}
}
