
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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.jkoolcloud.remora.advices.RemoraAdvice;
import com.jkoolcloud.remora.core.utils.ReflectionUtils;

public enum AdviceRegistry {
	INSTANCE;

	private List<RemoraAdvice> adviceList = new ArrayList<>(50);
	private Map<String, RemoraAdvice> adviceMap = new HashMap<>(50);

	public void report(List<RemoraAdvice> adviceList) {

		this.adviceList = adviceList;
		adviceMap = adviceList.stream()
				.collect(Collectors.toMap(entry -> entry.getClass().getSimpleName(), entry -> entry));
	}

	public List<RemoraAdvice> getRegisteredAdvices() {
		return adviceList;
	}

	public RemoraAdvice getAdviceByName(String name) throws ClassNotFoundException {
		if (!adviceMap.containsKey(name)) {
			throw new ClassNotFoundException();
		}
		return adviceMap.get(name);
	}

	public static List<String> getConfigurableFields(RemoraAdvice advice) {

		Class<?> aClass = advice.getClass();
		ArrayList<Field> declaredFields = ReflectionUtils.geAllDeclaredtFields(aClass);

		return declaredFields.stream()
				.filter(field -> field.isAnnotationPresent(RemoraConfig.Configurable.class)
						&& !field.getAnnotation(RemoraConfig.Configurable.class).configurableOnce())
				.map(field -> field.getName()).collect(Collectors.toList());
	}

	public static Map<String, Object> mapToCurrentValues(RemoraAdvice advice,
			List<String> availableConfigurationFields) {
		return availableConfigurationFields.stream().collect(Collectors.toMap(fName -> fName, fName -> {

			try {
				Field declaredField = ReflectionUtils.getFieldFromAllDeclaredFields(advice.getClass(), fName);
				declaredField.setAccessible(true);
				Object value = declaredField.get(advice);

				return value == null ? "null" : value;
			} catch (Exception e) {
				e.printStackTrace();
			}
			return "N/A";
		}));
	}
}
