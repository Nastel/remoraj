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

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

public class ApacheHttpClientHarness implements Harness {

	@Configurable
	public String url = "localhost";

	@Configurable
	public Integer port = 8080;

	@Configurable
	public String body = "";

	@Configurable
	public Method method = Method.GET;

	private CloseableHttpClient httpClient;

	public void setup() {
		httpClient = HttpClients.createDefault();

	}

	public void run() {
		HttpRequest request;
		switch (method) {
		case GET:
			request = new HttpGet();

			break;
		case POST:
			request = new HttpPost();
			break;
		default:
			throw new IllegalStateException("Unexpected value: " + method);
		}
		if (request instanceof HttpEntityEnclosingRequest) {
			try {
				((HttpEntityEnclosingRequest) request).setEntity(new StringEntity(body));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		HttpHost host = new HttpHost(url, port);
		try {
			httpClient.execute(host, request);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public enum Method {
		GET, POST
	}
}
