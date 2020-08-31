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

import com.jkoolcloud.remora.advices.BaseTransformers;
import com.jkoolcloud.remora.advices.Loggable;
import com.jkoolcloud.remora.advices.RemoraAdvice;
import com.jkoolcloud.remora.core.EntryDefinition;

public class LoggingAdviceListener implements AdviceListener {
	@Override
	public void onIntercept(RemoraAdvice adviceInstance, Object thiz, Method method) {

	}

	@Override
	public void onMethodFinished(RemoraAdvice adviceInstance, double elapseTime) {
		if (adviceInstance instanceof Loggable) {
			((Loggable) adviceInstance).getLogger().info("Exiting: {} {}", adviceInstance,
					adviceInstance.getClass().getName(), "after");
		}

	}

	@Override
	public void onAdviceError(RemoraAdvice adviceInstance, Throwable e) {

	}

	@Override
	public void onCreateEntity(Class<?> adviceClass, EntryDefinition entryDefinition) {

	}

	@Override
	public void onProcessed(BaseTransformers adviceInstance, Object thiz, Method method) {
		if (adviceInstance instanceof Loggable) {
			((Loggable) adviceInstance).getLogger().info("Entering: {} {} from {}", adviceInstance,
					adviceInstance.getClass().getSimpleName(), "before",
					thiz == null ? method.getDeclaringClass().getName() : thiz.getClass().getName());

		}
	}
}
