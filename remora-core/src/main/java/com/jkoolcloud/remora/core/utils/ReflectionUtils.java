package com.jkoolcloud.remora.core.utils;

import java.lang.reflect.Field;

public class ReflectionUtils {
	public static <T> T getFieldValue(String path, Object object, Class<T> expectedReturn) {
		String[] levels = path.split("\\.");

		Object workingObject = object;
		for (String level : levels) {
			try {
				Field field = _getField(level, workingObject);
				Object o = field.get(workingObject);
				workingObject = o;
			} catch (NoSuchFieldException e) {
				e.printStackTrace();
				break;
			} catch (IllegalAccessException e) {
				// we set it accessible
			}
		}
		if (expectedReturn.isInstance(workingObject)) {
			return (T) workingObject;
		} else {
			throw new IllegalArgumentException("Type mismatch");
		}

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

}
