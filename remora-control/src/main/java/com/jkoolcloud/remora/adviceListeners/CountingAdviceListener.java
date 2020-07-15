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

import com.jkoolcloud.remora.advices.RemoraAdvice;
import com.jkoolcloud.remora.advices.RemoraStatistic;
import com.jkoolcloud.remora.advices.ReportingAdviceListener;
import com.jkoolcloud.remora.core.EntryDefinition;

public class CountingAdviceListener implements ReportingAdviceListener {
	private final long resetTime;
	private RemoraStatistic statistic = new RemoraStatistic();

	public CountingAdviceListener() {
		resetTime = System.currentTimeMillis();

	}

	@Override
	public void onIntercept(RemoraAdvice adviceInstance, Object thiz, Method method) {

	}

	@Override
	public void onMethodFinished(Class<?> adviceClass, double elapseTime) {

	}

	@Override
	public void onAdviceError(RemoraAdvice adviceInstance, Throwable e) {

	}

	@Override
	public void onCreateEntity(Class<?> adviceClass, EntryDefinition entryDefinition) {
		statistic.incEventCreateCount();
	}

	public RemoraStatistic getAdviceStatistic() {
		return statistic;
	}

	@Override
	public Map<String, Object> report() {
		return new HashMap<String, Object>() {
			{
				put("resetTimestamp", resetTime);
				put("timeSinceLastResetSeconds", (System.currentTimeMillis() - resetTime) / 1000);
				put("invokeCount", statistic.getInvokeCount());
				put("eventCreateCount", statistic.getEventCreateCount());
				put("errorCount", statistic.getErrorCount());
			}
		};
	}
}
