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

package com.jkoolcloud.remora.advices;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;

import org.junit.BeforeClass;
import org.junit.Test;
import org.takes.facets.fork.RqRegex;
import org.takes.rs.RsPrint;
import org.tinylog.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jkoolcloud.remora.AdviceRegistry;
import com.jkoolcloud.remora.adviceListeners.CountingAdviceListener;
import com.jkoolcloud.remora.takes.TKStatistics;

public class RemoraControlAdviceTest {
	public static final String TEST_BODY = "{\n" + "\t\"advice\": \"Advice1\",\n" + "\t\"property\": \"test\",\n"
			+ "\t\"value\": \"test\"\n" + "}\n";

	@BeforeClass
	public static void setupLogger() {
		RemoraControlAdvice.logger = Logger.tag("TEST");
	}

	@Test
	public void testPropertiesChangeHandler2() throws IOException {
		ServerSocket socket = new ServerSocket(7366);

		RemoraAdvice[] advices = { new Advice1(), new Advice2() };
		AdviceRegistry.INSTANCE.report(Arrays.asList(advices));
		InetSocketAddress inetSocketAddress = new RemoraControlAdvice.AvailableInetSocketAddress(7366)
				.getInetSocketAddress();
		RemoraControlAdvice.startHttpServer(inetSocketAddress);

		makeRequest(inetSocketAddress);

	}

	private void makeRequest(InetSocketAddress inetSocketAddress) throws IOException {
		URL url = new URL("http://localhost:" + inetSocketAddress.getPort() + "/change");
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("POST");
		con.setRequestProperty("Content-Type", "application/json; utf-8");
		con.setRequestProperty("Accept", "application/json");
		con.setDoOutput(true);
		try (OutputStream out = con.getOutputStream()) {
			byte[] input = TEST_BODY.getBytes();
			out.write(input, 0, input.length);
			out.flush();
		}

		try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"))) {
			StringBuilder response = new StringBuilder();
			String responseLine = null;
			while ((responseLine = br.readLine()) != null) {
				response.append(responseLine.trim());
			}
			System.out.println(response.toString());
			assertEquals("OK", response.toString());
		}
	}

	@Test
	public void testStatisticsResponse() throws Exception {
		AdviceRegistry.INSTANCE.report(Collections.singletonList(new Advice1()));
		BaseTransformers.registerListener(CountingAdviceListener.class);
		String s = new RsPrint(
				new TKStatistics().act(new RqRegex.Fake("/statistics/(?<advice>[^/]+)", "/statistics/Advice1")))
						.printBody();
		JsonNode jsonNode = new ObjectMapper().readTree(s);
		JsonNode error = jsonNode.get("error");
		assertNull(error);
		System.out.println(s);
	}

}