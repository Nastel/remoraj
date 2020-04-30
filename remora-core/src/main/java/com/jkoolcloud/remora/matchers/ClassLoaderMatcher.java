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

package com.jkoolcloud.remora.matchers;

import net.bytebuddy.matcher.ElementMatcher;

public class ClassLoaderMatcher extends ElementMatcher.Junction.AbstractBase<ClassLoader> {

	private final String name;

	private ClassLoaderMatcher(String name) {
		this.name = name;
	}

	public static ElementMatcher.Junction.AbstractBase<ClassLoader> classLoaderWithName(String name) {
		return new ClassLoaderMatcher(name);

	}

	public static ElementMatcher.Junction.AbstractBase<ClassLoader> isReflectionClassLoader() {
		return new ClassLoaderMatcher("sun.reflect.DelegatingClassLoader");
	}

	@Override
	public boolean matches(ClassLoader target) {
		return target != null && name.equals(target.getClass().getName());
	}
}