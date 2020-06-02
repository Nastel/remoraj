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
		TkAdviceList tkAdviceList = new TkAdviceList();
		String jsonInString = TakesUtils.getBody(tkAdviceList.act(new RqFake()).body());
		JsonNode jsonNode = new ObjectMapper().readTree(jsonInString);
		System.out.println(jsonInString);
	}

	@Test
	public void testgetValueForKey() throws ParseException {
		assertEquals("Advice1", TakesUtils.getValueForKey("advice", TEST_BODY));
		assertEquals("test", TakesUtils.getValueForKey("property", TEST_BODY));
		assertEquals("test", TakesUtils.getValueForKey("value", TEST_BODY));
	}

}