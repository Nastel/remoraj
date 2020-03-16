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

import static net.bytebuddy.matcher.ElementMatchers.*;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;

import org.tinylog.Logger;
import org.tinylog.TaggedLogger;

import com.jkoolcloud.remora.RemoraConfig;
import com.jkoolcloud.remora.core.EntryDefinition;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

@TransparentAdvice
public class JDBCConnectionAdvice extends BaseTransformers implements RemoraAdvice {

	public static final String ADVICE_NAME = "JDBCConnectionAdvice";
	public static String[] INTERCEPTING_CLASS = { "java.sql.Connection" };
	public static String INTERCEPTING_METHOD = "prepare";

	@RemoraConfig.Configurable
	public static boolean enabled = true;
	@RemoraConfig.Configurable
	public static boolean load = true;
	@RemoraConfig.Configurable
	public static boolean logging = false;
	public static TaggedLogger logger;

	/**
	 * Method matcher intended to match intercepted class method/s to instrument. See (@ElementMatcher) for available
	 * method matches.
	 */

	private static ElementMatcher<? super MethodDescription> methodMatcher() {
		return nameStartsWith("prepare").and(returns(hasSuperType(named("java.sql.PreparedStatement"))))
				.and(takesArgument(0, String.class)).and(isPublic());
	}

	/**
	 * Type matcher should find the class intended for instrumentation See (@ElementMatcher) for available matches.
	 */

	@Override
	public ElementMatcher<TypeDescription> getTypeMatcher() {
		return not(isInterface()).and(hasSuperType(named(INTERCEPTING_CLASS[0])));
	}

	@Override
	public AgentBuilder.Transformer getAdvice() {
		return advice;
	}

	static AgentBuilder.Transformer.ForAdvice advice = new AgentBuilder.Transformer.ForAdvice()
			.include(JDBCConnectionAdvice.class.getClassLoader())//
			.include(RemoraConfig.INSTANCE.classLoader)//
			.advice(methodMatcher(), JDBCConnectionAdvice.class.getName());

	/**
	 * Advices before method is called before instrumented method code
	 *
	 * @param thiz
	 *            reference to method object
	 * @param method
	 *            instrumented method description
	 * @param ed
	 *            {@link EntryDefinition} for collecting ant passing values to
	 *            {@link com.jkoolcloud.remora.core.output.OutputManager}
	 * @param startTime
	 *            method startTime
	 *
	 */

	@Advice.OnMethodEnter
	public static void before(@Advice.This Object thiz, //
			@Advice.Argument(0) String sql, //
			@Advice.Origin Method method, //
			@Advice.Local("ed") EntryDefinition ed, //
			@Advice.Local("startTime") long startTime) {
		try {

			ed = getEntryDefinition(ed, JDBCConnectionAdvice.class, logging ? logger : null);

			if (logging) {
				logger.info("Entering: {} {} from {}.{}()", JDBCConnectionAdvice.class.getName(), "before",
						thiz.getClass().getName(), method.getName());
			}
			stackThreadLocal.get().push(ed);
			ed.addPropertyIfExist("SQL", sql);
			if (logging) {
				logger.debug("Adding SQL: {}", sql);
			}

		} catch (Throwable t) {
			handleAdviceException(t, ADVICE_NAME, logging ? logger : null);
		}
	}

	/**
	 * Method called on instrumented method finished.
	 *
	 * @param obj
	 *            reference to method object
	 * @param method
	 *            instrumented method description
	 * @param arguments
	 *            arguments provided for method
	 * @param exception
	 *            exception thrown in method exit (not caught)
	 * @param ed
	 *            {@link EntryDefinition} passed along the method (from before method)
	 * @param startTime
	 *            startTime passed along the method
	 */

	@Advice.OnMethodExit(onThrowable = Throwable.class)
	public static void after(@Advice.This Object obj, //
			@Advice.Origin Method method, //
			@Advice.AllArguments Object[] arguments, //
			// @Advice.Return Object returnValue, // //TODO needs separate Advice capture for void type
			@Advice.Thrown Throwable exception, @Advice.Local("ed") EntryDefinition ed, //
			@Advice.Local("startTime") long startTime) {
		try {
			stackThreadLocal.get().pop();
		} catch (Exception e) {

		}
	}

	@Override
	protected AgentBuilder.Listener getListener() {
		return new TransformationLoggingListener(logger);
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
