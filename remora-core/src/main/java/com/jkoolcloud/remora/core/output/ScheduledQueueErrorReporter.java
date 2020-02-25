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
	public static Exception lastException;
	public static long lastIndexAppender;
	private int lastReportedMemoryQueueErrorCount = 0;
	private int lastReportedPersistentQueueErrorCount = 0;

	private final TaggedLogger logger;
	private final ScheduledExecutorService scheduledExecutorService;

	public ScheduledQueueErrorReporter(TaggedLogger logger, Integer delay) {
		this.logger = logger;
		scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
		scheduledExecutorService.scheduleAtFixedRate(() -> {
			if (logger != null) {
				int newIntermediateErrorCount = intermediateQueueFailCount.get();
				if (newIntermediateErrorCount < lastReportedMemoryQueueErrorCount) {
					logger.error(
							// put all stats, queue size, etc
							"Failed to write to mem queue: failure count = {}", intermediateQueueFailCount.get());
					lastReportedMemoryQueueErrorCount = newIntermediateErrorCount;
				}
				int newPersistentErrorCount = chronicleQueueFailCount.get();
				if (newPersistentErrorCount > lastReportedPersistentQueueErrorCount) {
					logger.error(
							"Failed to write to persistent queue: failure count={}, last appender index = {}, error={} {}",
							chronicleQueueFailCount.get(), lastIndexAppender, lastException.getClass().getSimpleName(),
							lastException.getMessage());
					logger.debug(lastException);
					lastReportedPersistentQueueErrorCount = newPersistentErrorCount;
				}

			}
		}, 0, delay, TimeUnit.SECONDS);
	}

	private void shutdown() {
		scheduledExecutorService.shutdown();
	}
}
