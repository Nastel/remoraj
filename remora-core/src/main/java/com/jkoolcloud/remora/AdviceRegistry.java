
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

package com.jkoolcloud.remora;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import org.tinylog.Logger;
import org.tinylog.TaggedLogger;

import com.jkoolcloud.remora.advices.BaseTransformers;
import com.jkoolcloud.remora.advices.RemoraAdvice;
import com.jkoolcloud.remora.filters.AdviceFilter;
import com.jkoolcloud.remora.filters.FilterManager;
import com.jkoolcloud.remora.filters.LimitingFilter;

public enum AdviceRegistry {
	INSTANCE;

	public static final String AUTO_LIMITING_FILTER = "AUTO_LIMITING_FILTER";
	@RemoraConfig.Configurable
	public static int filterAdvance = 2;
	@RemoraConfig.Configurable
	public static int releaseTimeSec = 60;
	private List<RemoraAdvice> adviceList = new CopyOnWriteArrayList();
	private Map<String, RemoraAdvice> adviceMap = new ConcurrentHashMap<>(50);

	public void report(List<RemoraAdvice> adviceList) {

		this.adviceList = adviceList;
		adviceMap = adviceList.stream()
				.collect(Collectors.toMap(entry -> entry.getClass().getSimpleName(), entry -> entry));
	}

	public List<RemoraAdvice> getRegisteredAdvices() {
		return adviceList;
	}

	public RemoraAdvice getAdviceByName(String name) throws ClassNotFoundException {
		if (!adviceMap.containsKey(name)) {
			throw new ClassNotFoundException();
		}
		return adviceMap.get(name);
	}

	public BaseTransformers getBaseTransformerByName(String name) throws ClassNotFoundException {

		RemoraAdvice adviceByName = getAdviceByName(name);
		if (adviceByName instanceof BaseTransformers) {
			return (BaseTransformers) adviceByName;
		} else {
			throw new ClassNotFoundException();
		}
	}

	public static void limit() {
		LimitingFilter limitingFilter = (LimitingFilter) FilterManager.INSTANCE.get(AUTO_LIMITING_FILTER);
		if (limitingFilter == null) {

			limitingFilter = new LimitingFilter();
			limitingFilter.mode = AdviceFilter.Mode.INCLUDE;
			limitingFilter.everyNth = 1;
			FilterManager.INSTANCE.add(AUTO_LIMITING_FILTER, limitingFilter);
		}
		limitingFilter = (LimitingFilter) FilterManager.INSTANCE.get(AUTO_LIMITING_FILTER);
		LimitingFilter finalLimitingFilter = limitingFilter;
		AdviceRegistry.INSTANCE.adviceList.stream().filter(advice -> advice instanceof BaseTransformers)
				.filter(advice -> !((BaseTransformers) advice).filters.contains(finalLimitingFilter))
				.forEach(advice -> ((BaseTransformers) advice).filters.add(finalLimitingFilter));

		((LimitingFilter) limitingFilter).everyNth *= filterAdvance;

		scheduleRelease();
	}

	private static void scheduleRelease() {
		ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
		executorService.schedule(AdviceRegistry::release, releaseTimeSec, TimeUnit.SECONDS);
		executorService.shutdown();
	}

	public static void release() {
		try {
			LimitingFilter limitingFilter = (LimitingFilter) FilterManager.INSTANCE.get(AUTO_LIMITING_FILTER);
			limitingFilter.everyNth /= filterAdvance;
			if (limitingFilter.everyNth <= 1) {
				AdviceRegistry.INSTANCE.adviceList.stream().filter(advice -> advice instanceof BaseTransformers)
						.map(advice -> (BaseTransformers) advice)
						.forEach(advice -> advice.filters.remove(limitingFilter));
				limitingFilter.everyNth = 1;
			} else {
				scheduleRelease();
			}

		} catch (Exception e) {
			TaggedLogger init = Logger.tag("INIT");
			init.error("Cannot release filter");
		}

	}

}
