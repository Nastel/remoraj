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