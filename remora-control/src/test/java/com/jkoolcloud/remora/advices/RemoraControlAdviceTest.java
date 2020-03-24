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

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.URL;
import java.text.ParseException;
import java.util.Arrays;

import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jkoolcloud.remora.AdviceRegistry;
import com.jkoolcloud.remora.testClasses.Advice1;
import com.jkoolcloud.remora.testClasses.Advice2;
import com.sun.net.httpserver.HttpServer;

public class RemoraControlAdviceTest {

	public static final String TEST_BODY = "{\n" + "\t\"advice\": \"advice1\",\n" + "\t\"property\": \"enabled\",\n"
			+ "\t\"value\": \"false\"\n" + "}\n";

	@Test
	public void testFormatResponse() throws IOException {
		RemoraAdvice[] advices = { new Advice1(), new Advice2() };
		AdviceRegistry.INSTANCE.report(Arrays.asList(advices));
		ObjectMapper mapper = new ObjectMapper();
		String jsonInString = RemoraControlAdvice.formatResponse().toString();
		JsonNode jsonNode = mapper.readTree(jsonInString);
		System.out.println(jsonInString);
	}

	@Test
	public void testgetValueForKey() throws ParseException {
		assertEquals("advice1", RemoraControlAdvice.getValueForKey("advice", TEST_BODY));
		assertEquals("enabled", RemoraControlAdvice.getValueForKey("property", TEST_BODY));
		assertEquals("false", RemoraControlAdvice.getValueForKey("value", TEST_BODY));
	}

	@Test
	public void testPropertiesChangeHandler() throws IOException {
		ServerSocket socket = new ServerSocket(7366);
		InetSocketAddress inetSocketAddress = new RemoraControlAdvice.AvailableInetSocketAddress(7366)
				.getInetSocketAddress();
		RemoraControlAdvice.startHttpServer(inetSocketAddress);

		makeRequest(inetSocketAddress);

	}

	@Test
	public void testPropertiesChangeHandler2() throws IOException {
		ServerSocket socket = new ServerSocket(7366);
		InetSocketAddress inetSocketAddress = new RemoraControlAdvice.AvailableInetSocketAddress(7366)
				.getInetSocketAddress();
		RemoraControlAdvice.startHttpServer2(inetSocketAddress);

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
	public void testAdminReporter() throws IOException {
		HttpServer server = HttpServer.create(new InetSocketAddress(7736), 10);
		server.createContext("/", t -> {
			t.getRequestBody();
		});
		RemoraControlAdvice.AdminReporter reporter = new RemoraControlAdvice.AdminReporter("", 7667, "test");

	}
}