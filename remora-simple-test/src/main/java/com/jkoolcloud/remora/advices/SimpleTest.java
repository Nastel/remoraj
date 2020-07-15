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

import static net.bytebuddy.matcher.ElementMatchers.named;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;

import org.tinylog.Logger;
import org.tinylog.TaggedLogger;

import com.jkoolcloud.remora.RemoraConfig;
import com.jkoolcloud.remora.core.EntryDefinition;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.NamedElement;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

public class SimpleTest extends BaseTransformers {

	public static final String ADVICE_NAME = "SimpleTest";
	public static String[] INTERCEPTING_CLASS = { "lt.slabs.com.jkoolcloud.remora.JustATest" };
	public static String INTERCEPTING_METHOD = "instrumentedMethod";

	public static TaggedLogger logger;

	static AgentBuilder.Transformer.ForAdvice advice = new AgentBuilder.Transformer.ForAdvice()
			.include(SimpleTest.class.getClassLoader())//
			.include(RemoraConfig.INSTANCE.classLoader)//

			.advice(methodMatcher(), "com.jkoolcloud.remora.advices.SimpleTest");

	private static ElementMatcher.Junction<NamedElement> methodMatcher() {
		return named(INTERCEPTING_METHOD);
	}

	@Override
	public ElementMatcher<TypeDescription> getTypeMatcher() {
		return named(INTERCEPTING_CLASS[0]);
	}

	@Override
	public AgentBuilder.Transformer getAdvice() {
		return advice;
	}

	@Advice.OnMethodEnter
	public static void before(@Advice.This Object thiz, //
			@Advice.Argument(0) Object uri, //
			@Advice.Argument(1) Object arguments, //
			@Advice.Origin Method method, //
			@Advice.Local("ed") EntryDefinition ed, @Advice.Local("context") InterceptionContext ctx, //
			@Advice.Local("starttime") long starttime) //
	{
		try {
			ctx = prepareIntercept(SimpleTest.class, thiz, method, arguments);
			if (!ctx.intercept) {
				return;
			}

			logger.info("BEFORE METHOD CALL");
			logger.warn("WARN");
			logger.error("ERROR");
			logger.debug("DEBUG");
			logger.trace("TRACE");
			logger.trace(new Exception("Exception"));

			System.out.println("BEFORE METHOD CALL");
			ed = getEntryDefinition(ed, SimpleTest.class, ctx);

			starttime = fillDefaultValuesBefore(ed, stackThreadLocal, thiz, method, ctx);
			ed.addProperty("URI", uri.toString());
			ed.addProperty("Arg", arguments.toString());

		} catch (Throwable t) {
			// handleAdviceException(t, ctx.interceptorInstance, logging ? logger : null );
		}
	}

	@Advice.OnMethodExit(onThrowable = Throwable.class)
	public static void after(@Advice.This Object obj, //
			@Advice.Origin Method method, //
			@Advice.Thrown Throwable exception, //
			@Advice.Local("ed") EntryDefinition ed, @Advice.Local("context") InterceptionContext ctx, //
			@Advice.Local("starttime") long starttime) {
		try {
			ctx = prepareIntercept(SimpleTest.class, obj, method);
			if (!ctx.intercept) {
				return;
			}
			System.out.println("###AFTER METHOD CALL");
			// fillDefaultValuesAfter(ed, startTime, exception, logging ? logger : null );
		} finally {
			doFinally(ctx, obj.getClass());
		}

	}

	@Override
	public AgentBuilder.Listener getListener() {
		return new BaseTransformers.TransformationLoggingListener(logger);
	}

	@Override
	public String getName() {
		return ADVICE_NAME;
	}

	@Override
	public void install(Instrumentation inst) {
		logger = Logger.tag(ADVICE_NAME);
		getTransform().with(getListener()).installOn(inst);
	}
}
