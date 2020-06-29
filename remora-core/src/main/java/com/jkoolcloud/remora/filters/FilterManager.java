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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public enum FilterManager {
	INSTANCE;

	Map<String, StatisticEnabledFilter> filters = new HashMap<>(10);

	public List<AdviceFilter> get(List<?> list) {
		return filters.entrySet().stream().filter(entry -> list.contains(entry.getKey())).map(entry -> entry.getValue())
				.collect(Collectors.toList());
	}

	public void add(String filterName, StatisticEnabledFilter filter) {
		filters.put(filterName, filter);
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
