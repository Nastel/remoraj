/*
 *
 * Copyright (c) 2019-2020 NasTel Technologies, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of NasTel
 * Technologies, Inc. ("Confidential Information").  You shall not disclose
 * such Confidential Information and shall use it only in accordance with
 * the terms of the license agreement you entered into with NasTel
 * Technologies.
 *
 * NASTEL MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF
 * THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE, OR NON-INFRINGEMENT. NASTEL SHALL NOT BE LIABLE FOR ANY DAMAGES
 * SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
 * THIS SOFTWARE OR ITS DERIVATIVES.
 *
 * CopyrightVersion 1.0
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
