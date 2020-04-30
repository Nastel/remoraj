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

import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Deque;
import java.util.concurrent.*;

import org.jetbrains.annotations.NotNull;
import org.tinylog.Logger;
import org.tinylog.TaggedLogger;

import com.jkoolcloud.remora.Remora;
import com.jkoolcloud.remora.RemoraConfig;
import com.jkoolcloud.remora.core.EntryDefinition;

import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.queue.ExcerptAppender;
import net.openhft.chronicle.queue.RollCycles;
import net.openhft.chronicle.queue.impl.RollingChronicleQueue;
import net.openhft.chronicle.queue.impl.StoreFileListener;

public class ChronicleOutput implements OutputManager.AgentOutput<EntryDefinition> {

	public static final String DISABLE_PROXY_CODEGEN = "disableProxyCodegen";
	TaggedLogger logger = Logger.tag("INIT");

	private ChronicleQueue queue;
	@RemoraConfig.Configurable
	String queuePath = System.getProperty(Remora.REMORA_PATH) + "/queue";

	@RemoraConfig.Configurable
	RollCycles rollCycle = RollCycles.valueOf("HOURLY");

	@RemoraConfig.Configurable
	Long timeout = 5000L;

	@RemoraConfig.Configurable
	Integer keepQueueRolls = 2;

	@RemoraConfig.Configurable
	Integer intermediateQueueSize = 1000;

	@RemoraConfig.Configurable
	Integer workerSize = 2;

	@RemoraConfig.Configurable
	Integer errorReportingSchedule = 2;

	Deque<File> unusedQueues;

	private ExecutorService queueWorkers;

	@Override
	public void init() {
		if (System.getProperty(DISABLE_PROXY_CODEGEN) == null) {
			// Disable proxy codegen by default
			System.setProperty(DISABLE_PROXY_CODEGEN, "true");
		}

		ThreadFactory threadFactory = new ThreadFactory() {
			@Override
			public Thread newThread(@NotNull Runnable r) {
				logger.info("Creating new thread");

				ExcerptAppender threadAppender = queue.acquireAppender();
				if (threadAppender != null) {
					logger.info("Appender initialized");
				} else {
					logger.error("Appender failed");
				}

				return new ChronicleAppenderThread(r, threadAppender);
			}
		};

		File queueDir = Paths.get(queuePath).toFile();
		unusedQueues = new LinkedBlockingDeque<>(keepQueueRolls);
		File[] cq4s = queueDir.listFiles(new CQ4FileFilter());
		if (cq4s != null) {
			unusedQueues.addAll(Arrays.asList(cq4s));
		}

		logger.info("Writing to " + queueDir.getAbsolutePath());
		new ScheduledQueueErrorReporter(logger, errorReportingSchedule);

		queue = ChronicleQueue.singleBuilder(queueDir.getPath()).rollCycle(rollCycle).timeoutMS(timeout)
				.storeFileListener(new StoreFileListener() {

					@Override
					public void onReleased(int cycle, File file) {
						while (!unusedQueues.offer(file)) {
							unusedQueues.removeFirst().delete();
						}

					}
				}).build();

		if (queue != null) {
			logger.info("Queue initialized " + this);
			if (queue instanceof RollingChronicleQueue) {
				((RollingChronicleQueue) queue).storeForCycle(((RollingChronicleQueue) queue).cycle(), 0, false);
			}
		} else {
			logger.error("Queue failed");
		}

		queueWorkers = new ThreadPoolExecutor(workerSize, workerSize, 0, TimeUnit.MILLISECONDS,
				new ArrayBlockingQueue<>(intermediateQueueSize), threadFactory,
				(r, executor) -> ScheduledQueueErrorReporter.intermediateQueueFailCount.incrementAndGet());

	}

	@Override
	public void send(EntryDefinition entry) {
		if (entry.isFinished()) {
			queueWorkers.submit(entry.exit);
		} else {
			queueWorkers.submit(entry.entry);
		}

	}

	@Override
	public void shutdown() {
		queueWorkers.shutdown();
		logger.info("Shutting down chronicle queue:" + this);
		if (queue != null) {
			queue.close();
		}
	}

	private static class CQ4FileFilter implements FilenameFilter {
		@Override
		public boolean accept(File path, String name) {
			String nfn = name.toLowerCase();
			return nfn.endsWith(".cq4");
		}
	}

	public static class ChronicleAppenderThread extends Thread {
		private final ExcerptAppender threadAppender;

		public ExcerptAppender getAppender() {
			return threadAppender;
		}

		public ChronicleAppenderThread(Runnable r, ExcerptAppender appender) {
			super(r);

			this.threadAppender = appender;
		}
	}
}
