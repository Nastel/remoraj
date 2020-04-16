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

import java.io.File;

import org.takes.Request;
import org.takes.Response;
import org.takes.Take;
import org.takes.rs.RsText;

import com.jkoolcloud.remora.Remora;
import com.jkoolcloud.remora.core.output.ScheduledQueueErrorReporter;

public class TkQueueStatistics implements Take {
	public static final String QUEUE_STATISTICS_RESPONSE_BODY = "'{'\n" + //
			"  \"memQErrorCount\" : \"{0}\",\n" + //
			"  \"lastPersistQIndex\" : \"{1}\",\n" + //
			"  \"persistQErrorCount\" : \"{2}\",\n" + //
			"  \"lastException\": \"{3}\"\n" + //
			"  \"usableSpace\": \"{4}\"\n" + //

			"'}'";

	@Override
	public Response act(Request req) throws Exception {
		long usableSpace = 0;
		try {
			usableSpace = new File(System.getProperty(Remora.REMORA_PATH)).getUsableSpace();
		} catch (Throwable t) {

		}
		return new RsText(format(QUEUE_STATISTICS_RESPONSE_BODY, ScheduledQueueErrorReporter.intermediateQueueFailCount,
				ScheduledQueueErrorReporter.lastIndexAppender, ScheduledQueueErrorReporter.chronicleQueueFailCount,
				ScheduledQueueErrorReporter.lastException, usableSpace));
	}

}
