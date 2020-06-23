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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.jkoolcloud.remora.RemoraConfig;

public class ClassNameFilter extends StatisticEnabledFilter {

	@RemoraConfig.Configurable
	public List<String> classNames = new ArrayList<>();
	@RemoraConfig.Configurable
	public Mode mode = Mode.EXCLUDE;
	@RemoraConfig.Configurable
	public boolean regex = false;

	@Override
	public boolean maches(Object thiz, Method method, Object... arguments) {
		if (regex) {
			return classNames.stream().filter(query -> thiz.getClass().getName().matches(query)).findFirst()
					.isPresent();
		} else {
			return classNames.contains(thiz.getClass().getName());
		}
	}

	@Override
	public Mode getMode() {
		return mode;
	}

	@Override
	public String toString() {
		return FilterManager.INSTANCE.get(this);
	}
}
