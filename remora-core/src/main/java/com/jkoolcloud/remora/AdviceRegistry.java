
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

	public static Map<String, String> mapToCurrentValues(RemoraAdvice advice,
			List<String> availableConfigurationFields) {
		return availableConfigurationFields.stream().collect(Collectors.toMap(fName -> fName, fName -> {

			try {
				Field declaredField = ReflectionUtils.getFieldFromAllDeclaredFields(advice.getClass(), fName);
				declaredField.setAccessible(true);
				return String.valueOf(declaredField.get(advice));
			} catch (Exception e) {
				e.printStackTrace();
			}
			return "N/A";
		}));
	}
}
