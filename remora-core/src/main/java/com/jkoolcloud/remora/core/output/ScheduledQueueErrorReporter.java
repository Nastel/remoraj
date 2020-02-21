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

package com.jkoolcloud.remora.core.output;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.tinylog.TaggedLogger;

public class ScheduledQueueErrorReporter {

	public static AtomicInteger chronicleQueueFailCount = new AtomicInteger(0);
	public static AtomicInteger intermediateQueueFailCount = new AtomicInteger(0);
	private long lastReportedErrorCount = 0L;
	private final TaggedLogger logger;
	private final ScheduledExecutorService scheduledExecutorService;

	public ScheduledQueueErrorReporter(TaggedLogger logger, Integer delay) {
		this.logger = logger;
		scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
		scheduledExecutorService.scheduleAtFixedRate(() -> {
			if (logger != null) {
				int newErrorCount = chronicleQueueFailCount.get() + intermediateQueueFailCount.get();
				if (lastReportedErrorCount < newErrorCount) {
					logger.error(
							"Intermediate queue failure occurred. Failed write to intermediateQueue count: {}, failed write to chronicle queue count: {} ",
							intermediateQueueFailCount.get(), chronicleQueueFailCount.get());
					lastReportedErrorCount = newErrorCount;
				}
			}
		}, 0, delay, TimeUnit.SECONDS);
	}

	private void shutdown() {
		scheduledExecutorService.shutdown();
	}
}
