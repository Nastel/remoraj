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
import java.util.concurrent.ConcurrentHashMap;

import com.jkoolcloud.remora.AdviceRegistry;
import com.jkoolcloud.remora.advices.AdviceListener;
import com.jkoolcloud.remora.advices.RemoraAdvice;
import com.jkoolcloud.remora.advices.RemoraStatistic;
import com.jkoolcloud.remora.core.EntryDefinition;

public class CountingAdviceListener implements AdviceListener {
	ConcurrentHashMap<Class<?>, RemoraStatistic> adviceStatisticsMap = new ConcurrentHashMap<>();

	{
		for (RemoraAdvice advice : AdviceRegistry.INSTANCE.getRegisteredAdvices()) {
			adviceStatisticsMap.put(advice.getClass(), new RemoraStatistic());
		}
	}

	@Override
	public void onIntercept(Class<?> adviceClass, Object thiz, Method method) {
		try {
			adviceStatisticsMap.get(adviceClass).incInvokeCount();
		} catch (Throwable t) {
			System.out.println();
		}
	}

	@Override
	public void onAdviceError(Class<?> adviceClass, Throwable e) {
		try {
			adviceStatisticsMap.get(adviceClass).incErrorCount();
		} catch (Throwable t) {
			System.out.println();
		}
	}

	@Override
	public void onCreateEntity(Class<?> adviceClass, EntryDefinition entryDefinition) {
		try {
			adviceStatisticsMap.get(adviceClass).incEventCreateCount();
		} catch (Throwable t) {
			System.out.println();
		}
	}

	public ConcurrentHashMap<Class<?>, RemoraStatistic> getAdviceStatisticsMap() {
		return adviceStatisticsMap;
	}

}
