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

package com.jkoolcloud.remora.advices;

import static net.bytebuddy.matcher.ElementMatchers.isAnnotatedWith;
import static net.bytebuddy.matcher.ElementMatchers.named;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;

import org.tinylog.Logger;
import org.tinylog.TaggedLogger;

import com.jkoolcloud.remora.RemoraConfig;
import com.jkoolcloud.remora.core.CallStack;
import com.jkoolcloud.remora.core.EntryDefinition;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

public class SimpleTestConstructor extends BaseTransformers {

	public static final String ADVICE_NAME = "SimpleTestConstuctor";
	public static String[] INTERCEPTING_CLASS = { "lt.slabs.com.jkoolcloud.remora.JustATest2" };
	public static String INTERCEPTING_METHOD = "constructor";

	@RemoraConfig.Configurable
	public static boolean load = true;
	@RemoraConfig.Configurable
	public static boolean logging = false;
	public static TaggedLogger logger;

	public static ThreadLocal<CallStack<EntryDefinition>> stackThreadLocal = new ThreadLocal<>();

	static AgentBuilder.Transformer.ForAdvice advice = new AgentBuilder.Transformer.ForAdvice()
			.include(SimpleTestConstructor.class.getClassLoader())//
			.include(RemoraConfig.INSTANCE.classLoader)//

			.advice(methodMatcher(), "com.jkoolcloud.remora.advices.SimpleTestConstructor");

	private static ElementMatcher.Junction<MethodDescription> methodMatcher() {
		return isAnnotatedWith(named("lt.slabs.com.jkoolcloud.remora.onEnter"));
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
	public static void before(@Advice.AllArguments Object[] args, //
			@Advice.Local("ed") EntryDefinition ed, //
			@Advice.Origin Method method, //
			@Advice.This Object thiz, //
			@Advice.Local("startTime") long starttime) //
	{
		try {
			System.out.println("BEFORE METHOD CALL");
			ed = getEntryDefinition(ed, SimpleTestConstructor.class, logging ? logger : null);

			if (args != null && args[0] instanceof String) {

				System.out.println("OK");
			} else {
				System.out.println("NOK");
			}

			switch (method.getName()) {
			case "a":
				System.out.println("A");
				break;
			default:
				System.out.println("Default");
				break;

			}

			starttime = fillDefaultValuesBefore(ed, stackThreadLocal, thiz, method, logging ? logger : null);

		} catch (Throwable t) {
			System.out.println("Exception");
			logger.info("Exception");
			t.printStackTrace();
			handleAdviceException(t, ADVICE_NAME, logging ? logger : null);
		}
	}

	@Advice.OnMethodExit(onThrowable = Throwable.class)
	public static void after(@Advice.This Object obj, //
			@Advice.Origin Method method, //
			@Advice.AllArguments Object[] arguments, //
			@Advice.Thrown Throwable exception, //
			@Advice.Local("ed") EntryDefinition ed, //
			@Advice.Local("startTime") long startTime) {
		boolean doFinally = true;
		try {
			if (ed == null) { // ed expected to be null if not created by entry, that's for duplicates
				if (logging) {
					logger.info("EntryDefinition not exist, entry might be filtered out as duplicate or ran on test");
				}
				doFinally = false;
				return;
			}
			if (logging) {
				logger.info("Exiting: {} {}", SimpleTestConstructor.class.getName(), "after");
			}
			fillDefaultValuesAfter(ed, startTime, exception, logging ? logger : null);
		} catch (Throwable t) {
			handleAdviceException(t, ADVICE_NAME, logging ? logger : null);
		} finally {
			if (doFinally) {
				doFinally(logging ? logger : null, obj.getClass());
			}
		}

	}

	@Override
	public AgentBuilder.Listener getListener() {
		return new TransformationLoggingListener(logger);
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
