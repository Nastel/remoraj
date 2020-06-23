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

public interface AdviceFilter {

	void countExcluded();

	void countInvoked();

	boolean maches(Object thiz, Method method, Object... arguments);

	Mode getMode();

	default boolean intercept(Object thiz, Method method, Object... arguments) {
		countInvoked();
		boolean maches = maches(thiz, method, arguments);

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
		INCLUDE, EXCLUDE
	}

}
