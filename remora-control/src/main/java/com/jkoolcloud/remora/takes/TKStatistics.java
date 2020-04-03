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
