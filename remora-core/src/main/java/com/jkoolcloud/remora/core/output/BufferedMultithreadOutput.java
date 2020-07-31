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

import java.util.concurrent.*;

import org.tinylog.Logger;
import org.tinylog.TaggedLogger;

import com.jkoolcloud.remora.AdviceRegistry;
import com.jkoolcloud.remora.Remora;
import com.jkoolcloud.remora.RemoraConfig;
import com.jkoolcloud.remora.advices.BaseTransformers;
import com.jkoolcloud.remora.core.EntryDefinition;
import com.jkoolcloud.remora.filters.AdviceFilter;
import com.jkoolcloud.remora.filters.FilterManager;
import com.jkoolcloud.remora.filters.LimitingFilter;

public class BufferedMultithreadOutput implements AgentOutput<EntryDefinition> {

	@RemoraConfig.Configurable
	public static boolean limitingFilter = true;
	public static final String AUTO_LIMITING_FILTER = "AUTO_LIMITING_FILTER";
	@RemoraConfig.Configurable
	public static int filterAdvance = 2;
	@RemoraConfig.Configurable
	public static int releaseTimeSec = 60;
	@RemoraConfig.Configurable
	Integer intermediateQueueSize = 1000;

	@RemoraConfig.Configurable
	Integer workerSize = 2;

	@RemoraConfig.Configurable
	AgentOutput<EntryDefinition> output;

	static TaggedLogger logger = Logger.tag(Remora.MAIN_REMORA_LOGGER);

	private ExecutorService queueWorkers;
	private ArrayBlockingQueue<Runnable> workQueue;

	public static void limit() {

		LimitingFilter limitingFilter = (LimitingFilter) FilterManager.INSTANCE.get(AUTO_LIMITING_FILTER);
		if (limitingFilter == null) {

			limitingFilter = new LimitingFilter();
			limitingFilter.mode = AdviceFilter.Mode.INCLUDE;
			limitingFilter.everyNth = 1;
			FilterManager.INSTANCE.add(AUTO_LIMITING_FILTER, limitingFilter);
			logger.info("Created new limmiting filter");
		}
		limitingFilter = (LimitingFilter) FilterManager.INSTANCE.get(AUTO_LIMITING_FILTER);
		LimitingFilter finalLimitingFilter = limitingFilter;
		AdviceRegistry.INSTANCE.getRegisteredAdvices().stream().filter(advice -> advice instanceof BaseTransformers)
				.filter(advice -> !((BaseTransformers) advice).filters.contains(finalLimitingFilter))
				.forEach(advice -> ((BaseTransformers) advice).filters.add(finalLimitingFilter));

		((LimitingFilter) limitingFilter).everyNth *= filterAdvance;

		logger.info("-> Filter advance: {}", ((LimitingFilter) limitingFilter).everyNth);
		scheduleRelease();
	}

	private static void scheduleRelease() {
		ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
		executorService.schedule(BufferedMultithreadOutput::release, releaseTimeSec, TimeUnit.SECONDS);
		executorService.shutdown();
	}

	public static void release() {
		try {
			LimitingFilter limitingFilter = (LimitingFilter) FilterManager.INSTANCE.get(AUTO_LIMITING_FILTER);
			limitingFilter.everyNth /= filterAdvance;
			logger.info("<- Filter advance: {}", ((LimitingFilter) limitingFilter).everyNth);
			if (limitingFilter.everyNth <= 1) {
				AdviceRegistry.INSTANCE.getRegisteredAdvices().stream()
						.filter(advice -> advice instanceof BaseTransformers).map(advice -> (BaseTransformers) advice)
						.forEach(advice -> advice.filters.remove(limitingFilter));
				limitingFilter.everyNth = 1;
			} else {
				scheduleRelease();
			}

		} catch (Exception e) {
			TaggedLogger init = Logger.tag(Remora.MAIN_REMORA_LOGGER);
			init.error("Cannot release filter");
		}

	}

	@Override
	public void init() throws OutputException {
		if (output == null) {
			output = new ChronicleOutput();
		}
		output.init();
		workQueue = new ArrayBlockingQueue<>(intermediateQueueSize);
		queueWorkers = new ThreadPoolExecutor(workerSize, workerSize, 0, TimeUnit.MILLISECONDS, workQueue,
				output.getThreadFactory(), (r, executor) -> {
					ScheduledQueueErrorReporter.intermediateQueueFailCount.incrementAndGet();
					logger.warn("Limiting advices, Overfilled queue");
					if (limitingFilter) {
						limit();
					}
				});

	}

	public int getImQueueSize() {
		if (workQueue != null) {
			return workQueue.size();
		} else {
			return 0;
		}
	}

	@Override
	public void send(EntryDefinition entry) {
		queueWorkers.submit(() -> output.send(entry));
	}

	@Override
	public void shutdown() {
		queueWorkers.shutdown();
		logger.info("Shutting down chronicle queue:" + this);

	}

	@Override
	public ThreadFactory getThreadFactory() {
		return Executors.defaultThreadFactory();
	}
}
