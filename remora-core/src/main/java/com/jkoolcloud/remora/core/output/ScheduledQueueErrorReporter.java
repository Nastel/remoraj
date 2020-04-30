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
