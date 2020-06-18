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

package com.jkoolcloud.remora.adviceListeners;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import com.jkoolcloud.remora.advices.ReportingAdviceListener;
import com.jkoolcloud.remora.core.EntryDefinition;

public class TimingAdviceListener implements ReportingAdviceListener {
	AtomicInteger maxTime = new AtomicInteger();
	AtomicInteger minTime = new AtomicInteger();
	AverageTime avgTime = new AverageTime();

	@Override
	public void onIntercept(Class<?> adviceClass, Object thiz, Method method) {

	}

	@Override
	public void onMethodFinished(Class<?> adviceClass, double elapseTime) {
		maxTime.set((int) Math.max(maxTime.get(), elapseTime));
		if (minTime.get() == 0) {
			minTime.set((int) elapseTime);
		} else {
			minTime.set((int) Math.min(minTime.get(), elapseTime));
		}
		avgTime.append((int) elapseTime);
	}

	@Override
	public void onAdviceError(Class<?> adviceClass, Throwable e) {

	}

	@Override
	public void onCreateEntity(Class<?> adviceClass, EntryDefinition entryDefinition) {

	}

	@Override
	public Map<String, Object> report() {
		return new HashMap<String, Object>() {
			{
				put("minTimes", minTime);
				put("maxTime", maxTime);
				put("avgTime", avgTime);
				put("timeUnit", "ns");
			}
		};
	}

	protected static class AverageTime {

		public static final long PRECISSION = 100L;
		AtomicLong count = new AtomicLong();
		AtomicLong currentAverage = new AtomicLong();

		@Override
		public String toString() {
			return String.valueOf(get());
		}

		public float get() {
			return currentAverage.get() / PRECISSION;
		}

		public void append(int elapseTime) {

			long count = this.count.getAndIncrement();
			currentAverage.set((long) (((get() * count + elapseTime) / this.count.get()) * PRECISSION));
		}
	}
}
