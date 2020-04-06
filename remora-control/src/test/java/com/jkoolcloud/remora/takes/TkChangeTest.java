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

package com.jkoolcloud.remora.takes;

import static org.junit.Assert.assertEquals;

import java.text.ParseException;
import java.util.Arrays;

import org.junit.Test;
import org.takes.rq.RqFake;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jkoolcloud.remora.AdviceRegistry;
import com.jkoolcloud.remora.advices.Advice1;
import com.jkoolcloud.remora.advices.Advice2;
import com.jkoolcloud.remora.advices.RemoraAdvice;

public class TkChangeTest {
	public static final String TEST_BODY = "{\n" + "\t\"advice\": \"Advice1\",\n" + "\t\"property\": \"test\",\n"
			+ "\t\"value\": \"test\"\n" + "}\n";

	@Test
	public void testFormatResponse() throws Exception {
		RemoraAdvice[] advices = { new Advice1(), new Advice2() };
		AdviceRegistry.INSTANCE.report(Arrays.asList(advices));
		String jsonInString = TkChange.getBody(new TkAdviceList().act(new RqFake()).body());
		JsonNode jsonNode = new ObjectMapper().readTree(jsonInString);
		System.out.println(jsonInString);
	}

	@Test
	public void testgetValueForKey() throws ParseException {
		assertEquals("Advice1", TkChange.getValueForKey("advice", TEST_BODY));
		assertEquals("test", TkChange.getValueForKey("property", TEST_BODY));
		assertEquals("test", TkChange.getValueForKey("value", TEST_BODY));
	}

}