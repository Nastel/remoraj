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

import static net.bytebuddy.matcher.ElementMatchers.hasGenericSuperType;
import static net.bytebuddy.matcher.ElementMatchers.named;

import java.io.InputStream;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;

import org.tinylog.Logger;
import org.tinylog.TaggedLogger;

import com.jkoolcloud.remora.RemoraConfig;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

public class InputStreamReadAdvice extends BaseTransformers implements RemoraAdvice {

	public static final String ADVICE_NAME = "InputStreamReadAdvice";
	public static String[] INTERCEPTING_CLASS = { "java.io.InputStream" };
	public static String INTERCEPTING_METHOD = "read";

	@RemoraConfig.Configurable
	public static boolean logging = false;

	public static TaggedLogger logger;

	/**
	 * Method matcher intended to match intercepted class method/s to instrument. See (@ElementMatcher) for available
	 * method matches.
	 */

	private static ElementMatcher<? super MethodDescription> methodMatcher() {
		return named(INTERCEPTING_METHOD);
	}

	/**
	 * Type matcher should find the class intended for instrumentation See (@ElementMatcher) for available matches.
	 */

	@Override
	public ElementMatcher<TypeDescription> getTypeMatcher() {
		return hasGenericSuperType(named(INTERCEPTING_CLASS[0]));
	}

	@Override
	public AgentBuilder.Transformer getAdvice() {
		return advice;
	}

	static AgentBuilder.Transformer.ForAdvice advice = new AgentBuilder.Transformer.ForAdvice()
			.include(InputStreamReadAdvice.class.getClassLoader()).include(RemoraConfig.INSTANCE.classLoader)//
			.advice(methodMatcher(), InputStreamReadAdvice.class.getName());

	/**
	 * Advices before method is called before instrumented method code
	 *
	 * @param thiz
	 *            reference to method object
	 * @param arguments
	 *            arguments provided for method
	 * @param method
	 *            instrumented method description
	 *
	 */

	@Advice.OnMethodEnter
	public static void before(@Advice.This InputStream thiz, //
			@Advice.AllArguments Object[] arguments, //
			@Advice.Origin Method method//
	) {
		try {
			if (!intercept(InputStreamReadAdvice.class, thiz, method, arguments)) {
				return;
			}
			StreamStats streamStats = InputStreamManager.INSTANCE.get(thiz, logging ? logger : null, method);
			if (streamStats == null) {
				throw new IllegalStateException("Stream stats is null");
			}
			if (arguments == null || arguments.length == 0) {
				streamStats.advanceCount();
			} else if (arguments instanceof Object[] && arguments.length == 3) {
				streamStats.advanceCount((int) arguments[2]);
			} else if (arguments instanceof Object[] && arguments.length == 1) {
				streamStats.advanceCount(((Object[]) arguments[0]).length);
			}
		} catch (Throwable t) {
			handleAdviceException(t, ADVICE_NAME, logging ? logger : null);
		}

	}

	@Override
	protected AgentBuilder.Listener getListener() {
		return new BaseTransformers.TransformationLoggingListener(logger);
	}

	@Override
	public void install(Instrumentation instrumentation) {
		logger = Logger.tag(ADVICE_NAME);
		if (load) {
			getTransform().with(getListener()).installOn(instrumentation);
		} else {
			logger.info("Advice {} not enabled", getName());
		}
	}

	@Override
	public String getName() {
		return ADVICE_NAME;
	}

}
