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

import org.takes.Request;
import org.takes.Response;
import org.takes.Take;
import org.takes.rq.RqHeaders;
import org.takes.rs.RsWithHeaders;

public final class TkCorsAllowAll implements Take {
	/**
	 * Original take.
	 */
	private final Take origin;

	public TkCorsAllowAll(Take take) {
		origin = take;
	}

	@Override
	public Response act(Request req) throws Exception {
		Response response;
		String domain = new RqHeaders.Smart(new RqHeaders.Base(req)).single("origin", "");
		response = new RsWithHeaders(origin.act(req), "Access-Control-Allow-Credentials: true",
				// @checkstyle LineLengthCheck (1 line)
				"Access-Control-Allow-Methods: OPTIONS, GET, PUT, POST, DELETE, HEAD",
				"Access-Control-Allow-Origin: *");

		return response;
	}
}
