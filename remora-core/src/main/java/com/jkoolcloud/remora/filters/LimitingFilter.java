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
import java.util.concurrent.atomic.AtomicInteger;

import com.jkoolcloud.remora.RemoraConfig;

public class LimitingFilter implements AdviceFilter {
	AtomicInteger count = new AtomicInteger(0);

	@RemoraConfig.Configurable
	public Integer everyNth = 2;

	@RemoraConfig.Configurable
	public Mode mode = Mode.EXCLUDE;

	@Override
	public boolean maches(Object thiz, Method method, Object... arguments) {
		if (count.get() == 0) {
			count.set(everyNth);
		}
		int i = count.decrementAndGet();
		if (i == 0) {
			return false;
		} else {
			return true;
		}
	}

	@Override
	public Mode getMode() {
		return mode;
	}
}
