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

package com.jkoolcloud.remora.advices;

import java.lang.instrument.Instrumentation;

/**
 * Interface for Advices. Advices are RemoraJ classes related to {@link net.bytebuddy.asm.Advice} and supposedly have
 * definition of class to be instrumented and have methods annotated with {@link net.bytebuddy.asm.Advice.OnMethodExit}
 * and {@link net.bytebuddy.asm.Advice.OnMethodEnter}.
 *
 * Advice implementations are installed on {@link com.jkoolcloud.remora.RemoraInit} using
 * {@link java.util.ServiceLoader}. The implementations of actual should be defined in file
 * META-INF/services/com.jkoolcloud.remora.advices.RemoraAdvice witch contains a list of fully-qualified binary names of
 * concrete provider classes.
 */
public interface RemoraAdvice {
	void install(Instrumentation inst);

	String getName();
}
