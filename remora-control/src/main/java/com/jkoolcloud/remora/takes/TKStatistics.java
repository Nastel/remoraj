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

import static java.text.MessageFormat.format;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import org.takes.Request;
import org.takes.Response;
import org.takes.Take;
import org.takes.facets.fork.RqRegex;
import org.takes.rs.RsText;

import com.jkoolcloud.remora.AdviceRegistry;
import com.jkoolcloud.remora.advices.ReportingAdviceListener;

public class TKStatistics implements Take {

	public static final String STATISTICS_RESPONSE_BODY = "'{'\n" //
			+ "\t\"adviceName\" : \"{0}\",\n"//
			+ "\t{1}\n" // ReportingAdviceListener response
			+ "'}'";

	@Override
	public Response act(Request req) throws Exception {
		try {
			String advice = ((RqRegex) req).matcher().group("advice");
			if (advice == null) {
				throw new IllegalArgumentException();
			}
			try {
				Class<?> aClass = Class.forName("com.jkoolcloud.remora.advices." + advice);
				String otherProperties = AdviceRegistry.INSTANCE
						.getBaseTransformerByName(aClass.getSimpleName()).listeners.stream()
								.filter(a -> a instanceof ReportingAdviceListener).map(a -> (ReportingAdviceListener) a)
								.map(ReportingAdviceListener::report).map(Map::entrySet).flatMap(Collection::stream)
								.map(e -> JSONUtils.quote(e.getKey()) + " : " + JSONUtils.quote(e.getValue()))
								.collect(Collectors.joining(",\n"));

				otherProperties = JSONUtils.addPadding(1, otherProperties);
				return new RsText(format(STATISTICS_RESPONSE_BODY, advice, otherProperties));

			} catch (ClassNotFoundException e) {
				return new RsText("{\"error\": \"No such advice\"}");
			} catch (NullPointerException e) {
				return new RsText("{\"error\": \"No such advice statistic\"}");
			}
		} catch (IllegalArgumentException e) {
			return new RsText("{\"error\": \"No advice provided\"}");
		}

	}

}
