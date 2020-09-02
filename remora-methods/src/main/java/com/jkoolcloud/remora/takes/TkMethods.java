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

import static com.jkoolcloud.remora.takes.JSONUtils.quote;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.takes.Request;
import org.takes.Response;
import org.takes.rq.RqHref;
import org.takes.rq.RqMethod;
import org.takes.rs.RsText;

import com.jkoolcloud.remora.AdviceRegistry;
import com.jkoolcloud.remora.advices.MethodsAdvice;

public class TkMethods implements PluginTake {

	@Override
	public String getEnpointPath() {
		return "/methods?/?(?<method>[^/]+)";
	}

	@Override
	public Response act(Request req) throws Exception {
		String method = new RqMethod.Base(req).method();
		MethodsAdvice adviceByName = (MethodsAdvice) AdviceRegistry.INSTANCE
				.getAdviceByName(MethodsAdvice.class.getSimpleName());
		switch (method) {
		case RqMethod.POST:
		case RqMethod.PUT:
			String path = new RqHref.Base(req).href().path();
			Pattern pattern = Pattern.compile("/methods/(?<method>[^/]+)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
			Matcher matcher = pattern.matcher(path);
			// TODO check that corresponds to actual class pattern
			if (matcher.matches()) {
				String instrumentMethod = matcher.group("method");
				String classAndMethod = instrumentMethod.substring(instrumentMethod.length() - 2).equals("()")
						? instrumentMethod.substring(0, instrumentMethod.length() - 2) : instrumentMethod;
				adviceByName.classAndMethodList.add(classAndMethod);
				adviceByName.writeConfigurationFiles();

				classAndMethod.substring(0, classAndMethod.lastIndexOf("."));
				return new RsText("OK");
			} else {
				return new RsText("Pattern not matched");
			}

		case RqMethod.GET:
			return new RsText(adviceByName.classAndMethodList.stream().map(line -> quote(line + "()"))
					.collect(Collectors.joining(",\n\t", "[\n\t", "]")));
		default:
			return new RsText("Unknown method");
		}
	}
}
