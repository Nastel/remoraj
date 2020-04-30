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

import static net.bytebuddy.matcher.ElementMatchers.*;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.tinylog.Logger;
import org.tinylog.TaggedLogger;

import com.jkoolcloud.remora.RemoraConfig;
import com.jkoolcloud.remora.core.EntryDefinition;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

public class JBossAdvice extends BaseTransformers implements RemoraAdvice {

	public static final String ADVICE_NAME = "JBossService";
	public static String[] INTERCEPTING_CLASS = { "io.undertow.servlet.handlers.ServletHandler" };

	public static final String JBOSS_MODULES_SYSTEM_PKGS = "jboss.modules.system.pkgs";
	public static final String APM_BASE_PACKAGE = "com.jkoolcloud.remora, org.tinylog, net.bytebuddy";
	public static String INTERCEPTING_METHOD = "handleRequest";

	@RemoraConfig.Configurable
	public static boolean logging = false;
	public static TaggedLogger logger;
	static AgentBuilder.Transformer.ForAdvice advice = new AgentBuilder.Transformer.ForAdvice()
			.include(JBossAdvice.class.getClassLoader())//
			.include(RemoraConfig.INSTANCE.classLoader) //
			.advice(methodMatcher(), JBossAdvice.class.getName());

	/**
	 * Method matcher intended to match intercepted class method/s to instrument. See (@ElementMatcher) for available
	 * method matches.
	 */
	private static ElementMatcher<? super MethodDescription> methodMatcher() {
		return named("service").and(takesArgument(0, named("javax.servlet.ServletRequest")))
				.and(takesArgument(1, named("javax.servlet.ServletResponse")));
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
			@Advice.Argument(0) ServletRequest req, //
			@Advice.Argument(1) ServletResponse resp, //
			@Advice.Local("ed") EntryDefinition ed, //
			@Advice.Local("startTime") long startTime) {
		try {
			if (!getAdviceInstance(JBossAdvice.class).enabled) {
				return;
			}
			ed = getEntryDefinition(ed, JBossAdvice.class, logging ? logger : null);
			if (logging) {
				logger.info("Entering: {} {} from {}", JBossAdvice.class.getSimpleName(), "before",
						thiz.getClass().getName());
			}
			if (req != null) {
				try {
					// if (req.getServletContext() != null) {
					// ed.addProperty("Resource", req.getServletContext().getContextPath()); CANT USE
					// }
					ed.addPropertyIfExist("CLIENT", req.getRemoteAddr());
					ed.addPropertyIfExist("SERVER", req.getLocalName());
					if (stackThreadLocal.get().getServer() == null) {
						stackThreadLocal.get().setServer(req.getLocalName());
					}
				} catch (Throwable t) {
					logger.info("Some of req failed" + req);
				}

			} else {
				logger.info("## Request null");
			}
			startTime = fillDefaultValuesBefore(ed, stackThreadLocal, thiz, method, logging ? logger : null);
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
			@Advice.Argument(0) ServletRequest req, //
			@Advice.Argument(1) ServletResponse resp, //
			// @Advice.Return Object returnValue, // //TODO needs separate Advice capture for void type
			@Advice.Thrown Throwable exception, @Advice.Local("ed") EntryDefinition ed, //
			@Advice.Local("startTime") long startTime) {
		boolean doFinally = true;
		try {
			if (!getAdviceInstance(JBossAdvice.class).enabled) {
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
				logger.info("Exiting: {} {}", JBossAdvice.class.getName(), "after");
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
		return not(isInterface()).and(hasSuperType(named("javax.servlet.Servlet")));
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
		// getTransform().with(getListener()).installOn(inst);

	}
}
