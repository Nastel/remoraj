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

import static junit.framework.TestCase.assertEquals;

import java.util.Collections;
import java.util.List;

import org.junit.Test;

import com.jkoolcloud.remora.advices.Advice1;

public class AdviceRegistryTest {

	@Test
	public void getConfigurableFields() {
		Advice1 testAdvice = new Advice1();
		AdviceRegistry.INSTANCE.report(Collections.singletonList(testAdvice));
		List<String> configurableFields = AdviceRegistry.getConfigurableFields(testAdvice);
		System.out.println(configurableFields);
		assertEquals(6, configurableFields.size());
	}
}