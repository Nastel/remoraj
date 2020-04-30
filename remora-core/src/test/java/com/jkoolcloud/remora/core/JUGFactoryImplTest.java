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

package com.jkoolcloud.remora.core;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

@State(Scope.Benchmark)
public class JUGFactoryImplTest {

	// @Test
	// public void testJUGtoStringOptions() {
	// CaliperMain.main(getClass(), new String[0]);
	//
	// }

	@Benchmark
	public String usingUUIDToString() {
		return JUGFactoryImpl.newUUID();

	}

	@Benchmark
	public String usingCustomMethod() {
		return JUGFactoryImpl.toString(JUGFactoryImpl.getUUID());

	}

}