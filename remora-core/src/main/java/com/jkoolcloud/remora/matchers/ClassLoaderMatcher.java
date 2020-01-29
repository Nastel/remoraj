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