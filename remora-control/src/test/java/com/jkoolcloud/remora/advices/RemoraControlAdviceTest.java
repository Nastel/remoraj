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

import org.junit.BeforeClass;
import org.junit.Test;
import org.tinylog.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jkoolcloud.remora.AdviceRegistry;
import com.jkoolcloud.remora.testClasses.Advice1;
import com.jkoolcloud.remora.testClasses.Advice2;

public class RemoraControlAdviceTest {

	public static final String TEST_BODY = "{\n" + "\t\"advice\": \"Advice1\",\n" + "\t\"property\": \"test\",\n"
			+ "\t\"value\": \"test\"\n" + "}\n";

	@BeforeClass
	public static void setupLogger() {
		RemoraControlAdvice.logger = Logger.tag("TEST");
	}

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
		assertEquals("Advice1", RemoraControlAdvice.getValueForKey("advice", TEST_BODY));
		assertEquals("test", RemoraControlAdvice.getValueForKey("property", TEST_BODY));
		assertEquals("test", RemoraControlAdvice.getValueForKey("value", TEST_BODY));
	}

	@Test
	public void testPropertiesChangeHandler2() throws IOException {
		ServerSocket socket = new ServerSocket(7366);

		RemoraAdvice[] advices = { new Advice1(), new Advice2() };
		AdviceRegistry.INSTANCE.report(Arrays.asList(advices));
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

	// @Test
	// public void testAdminReporter() throws IOException {
	// new FtBasic(//
	// new TkFork(//
	// new FkRegex("/", ,
	// );
	// RemoraControlAdvice.AdminReporter reporter = new RemoraControlAdvice.AdminReporter("localhost", 7667, "test");
	//
	// }
}