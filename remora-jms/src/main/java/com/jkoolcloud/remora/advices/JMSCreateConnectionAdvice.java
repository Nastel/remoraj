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

import static net.bytebuddy.matcher.ElementMatchers.hasSuperType;
import static net.bytebuddy.matcher.ElementMatchers.named;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.util.Properties;

import javax.jms.ConnectionFactory;

import org.tinylog.Logger;
import org.tinylog.TaggedLogger;

import com.jkoolcloud.remora.RemoraConfig;
import com.jkoolcloud.remora.core.EntryDefinition;
import com.jkoolcloud.remora.core.utils.ReflectionUtils;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.NamedElement;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

public class JMSCreateConnectionAdvice extends BaseTransformers implements RemoraAdvice {

	public static final String ADVICE_NAME = "JMSCreateConnectionAdvice";
	public static String[] INTERCEPTING_CLASS = { "javax.jms.ConnectionFactory" };
	public static String INTERCEPTING_METHOD = "createConnection";

	/**
	 * Method matcher intended to match intercepted class method/s to instrument. See (@ElementMatcher) for available
	 * method maches.
	 */

	private static ElementMatcher.Junction<NamedElement> methodMatcher() {
		return named(INTERCEPTING_METHOD).or(named("createQueueConnection"));
	}

	static AgentBuilder.Transformer.ForAdvice advice = new AgentBuilder.Transformer.ForAdvice()
			.include(JMSCreateConnectionAdvice.class.getClassLoader())//
			.include(RemoraConfig.INSTANCE.classLoader) //
			.advice(methodMatcher(), JMSCreateConnectionAdvice.class.getName());

	/**
	 * Type matcher should find the class intended for intrumentation See (@ElementMatcher) for available matches.
	 */

	@Override
	public ElementMatcher<TypeDescription> getTypeMatcher() {
		return hasSuperType(named(INTERCEPTING_CLASS[0]));
	}

	@Override
	public AgentBuilder.Transformer getAdvice() {
		return advice;
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
	 * 
	 */

	@Advice.OnMethodEnter
	public static void before(@Advice.This ConnectionFactory thiz, //
			@Advice.AllArguments Object[] arguments, //
			@Advice.Origin Method method, //
			@Advice.Local("ed") EntryDefinition ed, @Advice.Local("context") InterceptionContext ctx, //
			@Advice.Local("startTime") long startTime) //
	// @Advice.Local("remoraLogger") Logger logger) //
	{
		try {
			ctx = prepareIntercept(JMSCreateConnectionAdvice.class, thiz, method, arguments);
			if (!ctx.intercept) {
				return;
			}
			TaggedLogger logger = ctx.interceptorInstance.getLogger();

			ed = getEntryDefinition(ed, JMSCreateConnectionAdvice.class, ctx);
			ed.setEventType(EntryDefinition.EventType.OPEN);

			startTime = fillDefaultValuesBefore(ed, stackThreadLocal, thiz, method, ctx);

			try {
				Properties fieldValue = ReflectionUtils.getFieldValue(thiz, Properties.class, "factory.properties");
				if (fieldValue != null) {
					ed.addProperties(fieldValue);
				}
			} catch (IllegalArgumentException e) {

			}

		} catch (Throwable t) {
			handleAdviceException(t, ctx);
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
	public static void after(@Advice.This ConnectionFactory obj, //
			@Advice.Origin Method method, //
			@Advice.AllArguments Object[] arguments, //
			@Advice.Thrown Throwable exception, //
			@Advice.Local("ed") EntryDefinition ed, @Advice.Local("context") InterceptionContext ctx, //
			@Advice.Local("startTime") long startTime)//
	// @Advice.Local("remoraLogger") Logger logger)
	{
		boolean doFinally = true;
		try {
			ctx = prepareIntercept(JMSCreateConnectionAdvice.class, obj, method, arguments);
			if (!ctx.intercept) {
				return;
			}
			TaggedLogger logger = ctx.interceptorInstance.getLogger();
			doFinally = checkEntryDefinition(ed, ctx);

			fillDefaultValuesAfter(ed, startTime, exception, ctx);
		} catch (Throwable t) {
			handleAdviceException(t, ctx);
		} finally {
			if (doFinally) {
				doFinally(ctx, obj.getClass());
			}
		}

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
		logger = Logger.tag(ADVICE_NAME);
		if (load) {
			getTransform().with(getListener()).installOn(inst);
		} else {
			logger.info("Advice {} not enabled", this, getName());
		}
	}
}
