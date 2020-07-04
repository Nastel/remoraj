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
			socket.close();
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
