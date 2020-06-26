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

import static com.jkoolcloud.remora.core.utils.ReflectionUtils.getFieldValue;
import static net.bytebuddy.matcher.ElementMatchers.*;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.sql.Statement;

import org.tinylog.Logger;
import org.tinylog.TaggedLogger;

import com.jkoolcloud.remora.RemoraConfig;
import com.jkoolcloud.remora.core.EntryDefinition;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

public class JDBCStatementAdvice extends BaseTransformers implements RemoraAdvice {

	public static final String ADVICE_NAME = "JDBCStatementAdvice";
	public static String[] INTERCEPTING_CLASS = { "java.sql.Statement" };
	public static String INTERCEPTING_METHOD = "execute";

	@RemoraConfig.Configurable
	public static boolean logging = false;
	public static TaggedLogger logger;

	/**
	 * Method matcher intended to match intercepted class method/s to instrument. See (@ElementMatcher) for available
	 * method matches.
	 */

	private static ElementMatcher<? super MethodDescription> methodMatcher() {
		return nameStartsWith("execute").and(takesArgument(0, String.class).or(takesArguments(0))).and(isPublic());
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
			.include(JDBCStatementAdvice.class.getClassLoader())//
			.include(RemoraConfig.INSTANCE.classLoader)//
			.advice(methodMatcher(), JDBCStatementAdvice.class.getName());

	/**
	 * Advices before method is called before instrumented method code
	 *
	 * @param thiz
	 *            reference to method object
	 * @param arguments
	 *            arguments provided for method
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
	public static void before(@Advice.This Statement thiz, //
			@Advice.AllArguments Object[] arguments, //
			@Advice.Origin Method method, //
			@Advice.Local("ed") EntryDefinition ed, //
			@Advice.Local("startTime") long startTime) {
		try {
			// if (isChainedClassInterception(JDBCStatementAdvice.class, logging ? logger : null)) {
			// return;
			// }
			if (!intercept(JDBCStatementAdvice.class, thiz, method, logging ? logger : null, arguments)) {
				return;
			}
			ed = getEntryDefinition(ed, JDBCStatementAdvice.class, logging ? logger : null);
			if (logging) {
				logger.info("Entering: {} {} from {}.{}()", JDBCStatementAdvice.class.getName(), "before",
						thiz.getClass().getName(), method.getName());
			}
			startTime = fillDefaultValuesBefore(ed, stackThreadLocal, thiz, method, logging ? logger : null);

			String sql = ed.getProperties().get("SQL");

			if (arguments != null && arguments.length > 0 && arguments[0] instanceof String) {
				sql = (String) arguments[0];
			}

			if (sql != null) {
				ed.addPropertyIfExist("SQL", sql);

				if (sql.toUpperCase().startsWith("SELECT")) {
					ed.setEventType(EntryDefinition.EventType.RECEIVE);
				}

				if (sql.toUpperCase().startsWith("UPDATE") || sql.toUpperCase().startsWith("INSERT")) {
					ed.setEventType(EntryDefinition.EventType.SEND);
				}
			}

			try {
				try {
					String resource = getFieldValue(thiz, String.class, "connection.myURL", "wrappedConn.mc.myURL",
							"jndiName");
					ed.setResource(resource, EntryDefinition.ResourceType.DATABASE);
					ed.addPropertyIfExist("RESOURCE", resource);
					if (logging) {
						logger.info("Adding resource reflection {}", resource);
					}
				} catch (IllegalArgumentException e) {
				}

			} catch (Exception e1) {
				if (logging) {
					logger.info("Exception: {}", e1);
				}
			}

		} catch (Throwable t) {
			handleAdviceException(t, ADVICE_NAME, logging ? logger : null);
		}
	}

	/**
	 * Method called on instrumented method finished.
	 *
	 * @param thiz
	 *            reference to method object
	 * @param method
	 *            instrumented method description
	 * @param exception
	 *            exception thrown in method exit (not caught)
	 * @param ed
	 *            {@link EntryDefinition} passed along the method (from before method)
	 * @param startTime
	 *            startTime passed along the method
	 */

	@Advice.OnMethodExit(onThrowable = Throwable.class)
	public static void after(@Advice.This Statement thiz, //
			@Advice.Origin Method method, //
			// @Advice.Return Object returnValue, // //TODO needs separate Advice capture for void type
			@Advice.Thrown Throwable exception, @Advice.Local("ed") EntryDefinition ed, //
			@Advice.Local("startTime") long startTime) {
		boolean doFinally = true;
		try {
			if (!intercept(JDBCStatementAdvice.class, thiz, method, logging ? logger : null)) {
				return;
			}
			if (ed == null) { // ed expected to be null if not created by entry, that's for duplicates
				if (logging) {
					logger.info("EntryDefinition not exist, entry might be filtered out as duplicate or ran on test");
				}
				doFinally = false;
				return;
			}
			if (logging) {
				logger.info("Exiting: {} {}", JDBCStatementAdvice.class.getName(), "after");
			}
			fillDefaultValuesAfter(ed, startTime, exception, logging ? logger : null);

		} catch (Throwable t) {
			handleAdviceException(t, ADVICE_NAME, logging ? logger : null);
		} finally {
			if (doFinally) {
				doFinally(logging ? logger : null, thiz.getClass());
			}
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
