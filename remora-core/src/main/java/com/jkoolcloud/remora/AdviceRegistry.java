
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.jkoolcloud.remora.advices.BaseTransformers;
import com.jkoolcloud.remora.advices.RemoraAdvice;
import com.jkoolcloud.remora.filters.AdviceFilter;
import com.jkoolcloud.remora.filters.FilterManager;
import com.jkoolcloud.remora.filters.LimitingFilter;

public enum AdviceRegistry {
	INSTANCE;

	public static final String AUTO_LIMITING_FILTER = "AUTO_LIMITING_FILTER";
	public static final int FILTER_ADVANCE = 2;
	public static final int INITIAL_FILTER_EVERY_N_TH = 256;
	public static final int RELEASE_TIME_SEC = 60;
	private List<RemoraAdvice> adviceList = new ArrayList<>(50);
	private Map<String, RemoraAdvice> adviceMap = new HashMap<>(50);

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
			limitingFilter.everyNth = INITIAL_FILTER_EVERY_N_TH * FILTER_ADVANCE;
			FilterManager.INSTANCE.add(AUTO_LIMITING_FILTER, limitingFilter);
		}
		AdviceFilter filter = FilterManager.INSTANCE.get(AUTO_LIMITING_FILTER);
		AdviceRegistry.INSTANCE.adviceList.stream()
				.filter(advice -> !((BaseTransformers) advice).filters.contains(filter))
				.forEach(advice -> ((BaseTransformers) advice).filters.add(filter));

		((LimitingFilter) limitingFilter).everyNth /= FILTER_ADVANCE;
		ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

		executorService.scheduleAtFixedRate(AdviceRegistry::release, 0, RELEASE_TIME_SEC, TimeUnit.SECONDS);
	}

	public static void release() {
		LimitingFilter limitingFilter = (LimitingFilter) FilterManager.INSTANCE.get(AUTO_LIMITING_FILTER);
		limitingFilter.everyNth *= FILTER_ADVANCE;
		if (limitingFilter.everyNth > INITIAL_FILTER_EVERY_N_TH) {
			AdviceRegistry.INSTANCE.adviceList.stream().map(advice -> (BaseTransformers) advice)
					.forEach(advice -> advice.filters.remove(limitingFilter));
		}
	}

}
