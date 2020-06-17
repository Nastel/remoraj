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

import com.jkoolcloud.remora.advices.RemoraStatistic;
import com.jkoolcloud.remora.advices.ReportingAdviceListener;
import com.jkoolcloud.remora.core.EntryDefinition;

public class CountingAdviceListener implements ReportingAdviceListener {
	private RemoraStatistic statistic = new RemoraStatistic();

	@Override
	public void onIntercept(Class<?> adviceClass, Object thiz, Method method) {
		statistic.incInvokeCount();
	}

	@Override
	public void onMethodFinished(Class<?> adviceClass, double elapseTime) {

	}

	@Override
	public void onAdviceError(Class<?> adviceClass, Throwable e) {
		statistic.incErrorCount();
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
				put("invokeCount", statistic.getInvokeCount());
				put("eventCreateCount", statistic.getEventCreateCount());
				put("errorCount", statistic.getErrorCount());
			}
		};
	}
}
