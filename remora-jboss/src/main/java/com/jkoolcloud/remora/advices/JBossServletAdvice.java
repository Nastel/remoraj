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

import static com.jkoolcloud.remora.advices.JBossAdvice.APM_BASE_PACKAGE;
import static com.jkoolcloud.remora.advices.JBossAdvice.JBOSS_MODULES_SYSTEM_PKGS;
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

public class JBossServletAdvice extends BaseTransformers implements RemoraAdvice {

	public static final String ADVICE_NAME = "JBoss";
	public static String[] INTERCEPTING_CLASS = { "io.undertow.servlet.handlers.ServletHandler" };

	public static String INTERCEPTING_METHOD = "handleRequest";

	@RemoraConfig.Configurable
	public static boolean logging = false;
	public static TaggedLogger logger;
	static AgentBuilder.Transformer.ForAdvice advice = new AgentBuilder.Transformer.ForAdvice()
			.include(JBossServletAdvice.class.getClassLoader())//
			.include(RemoraConfig.INSTANCE.classLoader) //
			.advice(methodMatcher(), JBossServletAdvice.class.getName());

	/**
	 * Method matcher intended to match intercepted class method/s to instrument. See (@ElementMatcher) for available
	 * method matches.
	 */
	private static ElementMatcher.Junction<NamedElement> methodMatcher() {
		return named(INTERCEPTING_METHOD);
	}

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
	 */
	@Advice.OnMethodEnter
	public static void before(@Advice.This Object thiz, //
			@Advice.AllArguments Object[] arguments, //
			@Advice.Origin Method method, //
			@Advice.Local("ed") EntryDefinition ed, //
			@Advice.Local("startTime") long startTime) {
		try {
			if (!intercept(JBossServletAdvice.class, thiz, method, arguments)) {
				return;
			}
			ed = getEntryDefinition(ed, JBossServletAdvice.class, logging ? logger : null);
			if (logging) {
				logger.info("Entering: {} {} from {}", JBossServletAdvice.class.getSimpleName(), "before",
						thiz.getClass().getName());
			}
			startTime = fillDefaultValuesBefore(ed, stackThreadLocal, thiz, method, logging ? logger : null);

			// Class find issues
			// if (arguments !=null && arguments.length > 0 &&
			// arguments.getClass().isInstance(Class.forName("io.undertow.servlet.core.ManagedServlet"))) {
			// ed.addPropertyIfExist("RESOURCE", arguments[0].toString());
			// }
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
		boolean doFinally = true;
		try {
			if (!intercept(JBossServletAdvice.class, obj, method, arguments)) {
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
				logger.info("Exiting: {} {}", JBossServletAdvice.class.getName(), "after");
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

	/**
	 * Type matcher should find the class intended for instrumentation See (@ElementMatcher) for available matches.
	 */
	@Override
	public ElementMatcher<TypeDescription> getTypeMatcher() {
		return named(INTERCEPTING_CLASS[0]);
	}

	@Override
	public AgentBuilder.Transformer getAdvice() {
		return advice;
	}

	@Override
	protected AgentBuilder.Listener getListener() {
		return new TransformationLoggingListener(logger);
	}

	@Override
	public String getName() {
		return ADVICE_NAME;
	}

	@Override
	public void install(Instrumentation inst) {
		String systemPackages = System.getProperty(JBOSS_MODULES_SYSTEM_PKGS);
		if (systemPackages != null) {
			if (!systemPackages.contains(APM_BASE_PACKAGE)) {
				System.setProperty(JBOSS_MODULES_SYSTEM_PKGS, systemPackages + "," + APM_BASE_PACKAGE);
			}
		} else {
			System.setProperty(JBOSS_MODULES_SYSTEM_PKGS, APM_BASE_PACKAGE);
		}
		logger = Logger.tag(ADVICE_NAME);
		getTransform().with(getListener()).installOn(inst);

	}
}
