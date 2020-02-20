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

package com.jkoolcloud.testHarness.harnesses;

import java.lang.reflect.Field;

public abstract class MeasurableHarness implements Harness {

	@Override
	public HarnessResult call() throws Exception {
		HarnessResult harnessResult = new HarnessResult();
		harnessResult.start();
		harnessResult.setResult(call_());
		harnessResult.end();
		return harnessResult;

	}

	abstract String call_() throws Exception;

	@Override
	public String toString() {
		Class workngClass = getClass();
		StringBuilder result = new StringBuilder();
		result.append("<H4>");
		result.append(getClass().getSimpleName());
		result.append("</H4>");
		while (!workngClass.equals(Object.class)) {
			INNER: for (Field field : workngClass.getDeclaredFields()) {

				if (!field.isAnnotationPresent(Configurable.class)) {
                    continue INNER;
                }
				result.append(field.getName());
				result.append("=");
				try {
					result.append(field.get(this));

				} catch (IllegalAccessException e) {
					result.append("N/A");
				}
				result.append("<br>");
			}
			workngClass = workngClass.getSuperclass();
		}
		return result.toString();
	}
}
