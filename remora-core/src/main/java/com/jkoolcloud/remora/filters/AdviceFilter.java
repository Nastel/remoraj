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

/**
 * Interface for Advice filter. Advice filter is called by
 * {@link com.jkoolcloud.remora.advices.BaseTransformers#prepareIntercept(Class, Object, Method, Object...)} and a
 * filter implementation is supposed to decide if the implementation is supposed to be processed further.
 * <p>
 * As the advice filter is supposed to be created by {@link com.jkoolcloud.remora.RemoraConfig} or REST endpoint on
 * remora-config module it should have default not arguments constructor.
 * <p>
 * For a filter configuration use fields annotated with {@link com.jkoolcloud.remora.RemoraConfig.Configurable}, so it
 * can be altered using REST and configuration file.
 */
public interface AdviceFilter {

	void countExcluded();

	void countInvoked();

	boolean excludeWholeStack();

	/**
	 * For a filter you need to override `matches` method, matches should return true if a filter maches. Method
	 * `matches` has a property `thiz` with a reference for intercepted object. NOTE that might be nul if intercepted
	 * method is static.
	 */

	boolean matches(Object thiz, Method method, Object... arguments);

	/**
	 * Filter has to modes `INCLUDE` and `EXCLUDE`, each advice calls default filters method
	 * {@link #intercept(Object, Method, Object...)} witch determine, by the mode set, if the intercept should be
	 * processed further.
	 *
	 * @return
	 */
	Mode getMode();

	/**
	 * default implementation of method making decision either the instrumentation should be processed further based on
	 * selected {@link Mode}. Filter can include everything that matches or, otherwise, exclude ones that matches.
	 * That's determined by mode. So the implementation itself is responsible only for determining if the filter
	 * matches.
	 */
	default boolean intercept(Object thiz, Method method, Object... arguments) {
		countInvoked();
		boolean maches = matches(thiz, method, arguments);

		if (getMode().equals(Mode.INCLUDE)) {
			if (!maches) {
				countExcluded();
			}
			return maches;
		}
		if (getMode().equals(Mode.EXCLUDE)) {
			if (maches) {
				countExcluded();
			}
			return !maches;
		}
		return true;
	}

	enum Mode {
		/**
		 * The filter includes <b>only</b> the interceptions matching filter.
		 */
		INCLUDE,
		/**
		 * The filters excludes from further processing if filter matches.
		 */
		EXCLUDE
	}

}
