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

import static com.jkoolcloud.remora.advices.MethodsAdvice.CONFIGURATION_PATH;
import static junit.framework.TestCase.assertTrue;
import static org.mockito.Mockito.mock;

import java.lang.instrument.Instrumentation;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.takes.rq.RqFake;
import org.takes.rq.RqMethod;
import org.takes.rs.RsPrint;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jkoolcloud.remora.AdviceRegistry;
import com.jkoolcloud.remora.advices.MethodsAdvice;

public class TKMethodsTest {

	@BeforeClass
	public static void setupENV() {
		System.setProperty("remora.path", "..");
		System.out.println(Paths.get(System.getProperty("remora.path")).toAbsolutePath().normalize());

	}

	@Test
	public void testActGET() throws Exception {
		MethodsAdvice advice = new MethodsAdvice();
		advice.install(mock(Instrumentation.class));

		AdviceRegistry.INSTANCE.report(Collections.singletonList(advice));
		String s = new RsPrint(new TKMethods().act(new RqFake())).printBody();
		JsonNode jsonNode = new ObjectMapper().readTree(s);
		System.out.println(s);
		// System.out.println(jsonNode);

	}

	@Test
	public void testActPOST() throws Exception {
		AdviceRegistry.INSTANCE.report(Collections.singletonList(new MethodsAdvice()));
		String s = new RsPrint(
				new TKMethods().act(new RqFake(RqMethod.PUT, "/methods/com.nastel.bank.DbUtils.getUserId()")))
						.printBody();
		String s1 = new RsPrint(
				new TKMethods().act(new RqFake(RqMethod.PUT, "/methods/com.nastel.bank.DbUtils.getBalance")))
						.printBody();
		// JsonNode jsonNode = new ObjectMapper().readTree(s);
		List<String> strings = Files.readAllLines(CONFIGURATION_PATH);
		assertTrue(strings.stream().anyMatch(line -> line.equals("com.nastel.bank.DbUtils.getUserId()")));
		assertTrue(strings.stream().anyMatch(line -> line.equals("com.nastel.bank.DbUtils.getBalance()")));
		// System.out.println(jsonNode);

	}
}
