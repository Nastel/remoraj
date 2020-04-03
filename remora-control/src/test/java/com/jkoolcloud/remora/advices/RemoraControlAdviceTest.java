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

package com.jkoolcloud.remora.advices;

import static org.junit.Assert.*;

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
		RemoraControlAdvice.adviceListener = new CountingAdviceListener();
		String s = new RsPrint(
				new TKStatistics().act(new RqRegex.Fake("/statistics/(?<advice>[^/]+)", "/statistics/Advice1")))
						.printBody();
		JsonNode jsonNode = new ObjectMapper().readTree(s);
		JsonNode error = jsonNode.get("error");
		assertNull(error);
		System.out.println(s);
	}

	@Test
	public void testStatisticsErrorResponse() throws Exception {
		AdviceRegistry.INSTANCE.report(Collections.singletonList(new Advice1()));
		RemoraControlAdvice.adviceListener = new CountingAdviceListener();
		String s = new RsPrint(
				new TKStatistics().act(new RqRegex.Fake("/statistics/(?<advice>[^/]+)", "/statistics/Advice2")))
						.printBody();
		JsonNode jsonNode = new ObjectMapper().readTree(s);
		JsonNode error = jsonNode.get("error");
		assertNotNull(error);
		System.out.println(s);
	}
}