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

import static java.text.MessageFormat.format;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.*;

import org.tinylog.TaggedLogger;

public class AdminReporter {

	private final int port;
	private final String name;
	private String REPORT_MESSAGE_TEMPLATE = "{\n" + "\t\"adress\": \"{}}\",\n" + "\t\"port\": {}},\n"
			+ "\t\"vmIdentification\": \"{}}\"\n" + "\t\"version\": \"{}}\"\n }";
	private String localAddress = null;
	private URL url;

	public AdminReporter(String address, int port, String name, TaggedLogger logger) {
		this.port = port;
		this.name = name;

		try {
			url = new URL(address);

			Socket socket = new Socket();
			socket.connect(new InetSocketAddress(url.getHost(), url.getPort()));
			InetAddress localAddress1 = socket.getLocalAddress();
			localAddress = localAddress1.getHostAddress();

			url = url;
		} catch (Exception e) {
			logger.error("Cannot initialize admin reporter: \n {}", e);
		}

	}

	public boolean report() {
		try {
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "application/json; utf-8");
			con.setRequestProperty("Accept", "application/json");
			con.setDoOutput(true);
			try (OutputStream out = con.getOutputStream()) {
				byte[] input = format(REPORT_MESSAGE_TEMPLATE, localAddress, port, name, Remora.getVersion())
						.getBytes();
				out.write(input, 0, input.length);
				out.flush();
			}

			try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"))) {
				StringBuilder response = new StringBuilder();
				String responseLine = null;
				while ((responseLine = br.readLine()) != null) {
					response.append(responseLine.trim());
				}
			}
		} catch (IOException e) {
			return false;
		}
		return true;
	}
}
