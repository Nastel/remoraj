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

import com.jkoolcloud.remora.advices.RemoraAdvice;
import com.jkoolcloud.remora.core.EntryDefinition;

public interface AdviceListener {
	void onIntercept(RemoraAdvice adviceInstance, Object thiz, Method method);

	void onMethodFinished(RemoraAdvice adviceClass, double elapseTime);

	void onAdviceError(RemoraAdvice adviceInstance, Throwable e);

	void onCreateEntity(Class<?> adviceClass, EntryDefinition entryDefinition);
}
