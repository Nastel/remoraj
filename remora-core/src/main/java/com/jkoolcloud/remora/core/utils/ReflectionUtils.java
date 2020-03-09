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

package com.jkoolcloud.remora.core.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

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
				if (method == null) {
				}
				try {
					method = object.getClass().getMethod(name);
				} catch (NoSuchMethodException e2) {
				}
			}

			method.setAccessible(true);
			ret = method.invoke(object);
		} catch (Exception e) {
		}
		return ret;
	}

}
