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

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;

import com.jkoolcloud.remora.advices.BaseTransformers;
import com.jkoolcloud.remora.advices.RemoraAdvice;
import com.jkoolcloud.remora.core.EntryDefinition;

/**
 * Listener for a method interception events. Listeners are registered using
 * {@link BaseTransformers#registerListener(Class)} method. Listener can be registered any time. For a custom listeners
 * it's adviceable to install listners using {@link RemoraAdvice#install(Instrumentation)} cause this quarantees to call
 * once during {@link java.util.ServiceLoader load} of remora modules.
 * <p>
 * Happypath sequence:
 * <p>
 * <ul>
 * <li>{@link #onIntercept(RemoraAdvice, Object, Method)}
 * <li>{@link #onProcessed(BaseTransformers, Object, Method)}
 * <li>{@link #onCreateEntity(Class, EntryDefinition)}
 * <li>Send {@link com.jkoolcloud.remora.core.Entry}
 * <li>{@link #onMethodFinished(RemoraAdvice, double)}
 * <li>Send {@link com.jkoolcloud.remora.core.Exit}
 * </ul>
 * <p>
 */

public interface AdviceListener {
	/**
	 * Callback for interception called on beggining of intercepted method <strong>before</strong> the filter is
	 * applied, or {@link BaseTransformers#enabled} takes effect. Next one in processing row is
	 * {@link #onProcessed(BaseTransformers, Object, Method)}
	 *
	 * @param adviceInstance
	 *            Interceptor instance.
	 * @param thiz
	 *            The object neing intercepted.
	 * @param method
	 *            Intercepted method.
	 */

	void onIntercept(RemoraAdvice adviceInstance, Object thiz, Method method);

	/**
	 * Callback for interception called on beggining of intercepted method <strong>after</strong> the filter is applied,
	 * and {@link BaseTransformers#enabled} takes effect.
	 *
	 * @param adviceInstance
	 *            Interceptor instance.
	 * @param thiz
	 *            The object being intercepted. NOTE that might be nul if intercepted method is static.
	 * @param method
	 *            Intercepted method.
	 */

	void onProcessed(BaseTransformers adviceInstance, Object thiz, Method method);

	/**
	 * Calback invoked in case of {@link EntryDefinition} is created. There is many cases when intercteption doesn't end
	 * up with an entry creation:
	 * <p>
	 * <ul>
	 * <li>The interception in filtered out. See {@link com.jkoolcloud.remora.filters.AdviceFilter}
	 * <li>The advice is not Enabled. See {@link BaseTransformers#enabled}
	 * <li>The advice is entered multipe times on particular callStack. See
	 * {{@link BaseTransformers#getEntryDefinition(EntryDefinition, Class, BaseTransformers.InterceptionContext)}}
	 * <li>The advice is anottated with {@link com.jkoolcloud.remora.advices.TransparentAdvice}
	 * </ul>
	 * <p>
	 *
	 * @param adviceClass
	 *            adviceClass
	 * @param entryDefinition
	 *            created entity
	 */

	void onCreateEntity(Class<?> adviceClass, EntryDefinition entryDefinition);

	/**
	 * Callback called on actual intercepted method reached return.
	 *
	 * @param adviceClass
	 *            adviceClass
	 * @param elapseTime
	 *            method durration
	 */

	void onMethodFinished(RemoraAdvice adviceClass, double elapseTime);

	/**
	 * Callback for interception executed in interceptor error on try-catch clause
	 *
	 * @param adviceInstance
	 * @param e
	 */
	void onAdviceError(RemoraAdvice adviceInstance, Throwable e);

}
