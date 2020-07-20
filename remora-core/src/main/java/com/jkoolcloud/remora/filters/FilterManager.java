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

package com.jkoolcloud.remora.filters;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.tinylog.Logger;
import org.tinylog.TaggedLogger;

import com.jkoolcloud.remora.AdviceRegistry;
import com.jkoolcloud.remora.Remora;
import com.jkoolcloud.remora.advices.BaseTransformers;

public enum FilterManager {
	INSTANCE;
	private final TaggedLogger logger = Logger.tag(Remora.MAIN_REMORA_LOGGER);

	Map<String, StatisticEnabledFilter> filters = new ConcurrentHashMap<>(10);

	public List<AdviceFilter> get(List<?> list) {
		return filters.entrySet().stream().filter(entry -> list.contains(entry.getKey())).map(entry -> entry.getValue())
				.collect(Collectors.toList());
	}

	public void add(String filterName, StatisticEnabledFilter filter) {
		StatisticEnabledFilter put = filters.put(filterName, filter);
		logger.info("Adding filter {}, {}", filterName, filter);
		if (put != null) {
			logger.info(
					"There was already filter with name {}, filter is replaced, but references for advices using old filter is cleared."
							+ " You should set the advices you want to use new filter");
			AdviceRegistry.INSTANCE.getRegisteredAdvices().stream().filter(advice -> advice instanceof BaseTransformers)
					.map(advice -> (BaseTransformers) advice)
					.forEach(baseTransformers -> baseTransformers.filters.remove(put));
		}
	}

	public void add(String filterName, AdviceFilter filter) {
		if (filter instanceof StatisticEnabledFilter) {
			add(filterName, filter);
		}
	}

	public AdviceFilter get(String filterName) {
		return filters.get(filterName);
	}

	public Map<String, StatisticEnabledFilter> getAll() {
		return filters;
	}

	public String get(AdviceFilter classNameFilter) {
		return filters.entrySet().stream().filter(entry -> entry.getValue().equals(classNameFilter))
				.map(e -> e.getKey()).findFirst().orElse("N/A");
	}
}
