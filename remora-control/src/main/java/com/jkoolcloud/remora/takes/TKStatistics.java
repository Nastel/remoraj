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

import org.takes.Response;
import org.takes.facets.fork.RqRegex;
import org.takes.facets.fork.TkRegex;
import org.takes.rs.RsText;

import com.jkoolcloud.remora.advices.RemoraControlAdvice;
import com.jkoolcloud.remora.advices.RemoraStatistic;

public class TKStatistics implements TkRegex {

	public static final String STATISTICS_RESPONSE_BODY = "'{'\n" + "  \"adviceName\" : \"{0}\",\n"
			+ "  \"invokeCount\" : \"{1}\",\n" + "  \"eventCreateCount\" : \"{2}\",\n" + "  \"errorCount\": \"{3}\"\n"
			+ "'}'";

	@Override
	public Response act(RqRegex req) throws Exception {
		try {
			String advice = req.matcher().group("advice");
			if (advice == null) {
				throw new IllegalArgumentException();
			}
			try {
				Class<?> aClass = Class.forName("com.jkoolcloud.remora.advices." + advice);
				RemoraStatistic remoraStatistic = RemoraControlAdvice.getAdviceListener().getAdviceStatisticsMap()
						.get(aClass);
				return new RsText(format(STATISTICS_RESPONSE_BODY, advice, remoraStatistic.getInvokeCount(),
						remoraStatistic.getEventCreateCount(), remoraStatistic.getErrorCount()));

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
