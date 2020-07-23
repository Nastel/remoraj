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

import java.lang.reflect.Method;
import java.text.MessageFormat;

import org.apache.kafka.clients.consumer.Consumer;
import org.tinylog.TaggedLogger;

import com.jkoolcloud.remora.RemoraConfig;
import com.jkoolcloud.remora.core.CallStack;
import com.jkoolcloud.remora.core.EntryDefinition;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

public class KafkaConsumerClientAdvice extends BaseTransformers {

	public static final String ADVICE_NAME = "KafkaConsumerClientAdvice";
	public static String[] INTERCEPTING_CLASS = { "org.apache.kafka.clients.consumer.Consumer" };
	public static String INTERCEPTING_METHOD = "poll";

	static AgentBuilder.Transformer.ForAdvice advice = new AgentBuilder.Transformer.ForAdvice()
			.include(KafkaConsumerClientAdvice.class.getClassLoader()).include(RemoraConfig.INSTANCE.classLoader)//
			.advice(methodMatcher(), KafkaConsumerClientAdvice.class.getName());

	/**
	 * Method matcher intended to match intercepted class method/s to instrument. See (@ElementMatcher) for available
	 * method matches.
	 */

	private static ElementMatcher<? super MethodDescription> methodMatcher() {
		return named(INTERCEPTING_METHOD);
	}

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
	public static void before(@Advice.This Consumer<?, ?> thiz, //
			@Advice.Origin Method method, //
			@Advice.Local("ed") EntryDefinition ed, @Advice.Local("context") InterceptionContext ctx, //
			@Advice.Local("startTime") long startTime) {
		try {
			ctx = prepareIntercept(KafkaConsumerClientAdvice.class, thiz, method);
			if (!ctx.intercept) {
				return;
			}
			TaggedLogger logger = ctx.interceptorInstance.getLogger();
			ed = getEntryDefinition(ed, KafkaConsumerClientAdvice.class, ctx);

			ed.setTransparent();
			startTime = fillDefaultValuesBefore(ed, stackThreadLocal, thiz, method, ctx);

			ed.setEventType(EntryDefinition.EventType.CALL);
			try {
				String clientId = getFieldValue(thiz, String.class, "clientId");
				String groupId = getFieldValue(thiz, String.class, "groupId");
				CallStack entryDefinitions = stackThreadLocal.get();
				if (entryDefinitions != null) {
					String application = MessageFormat.format("clientId={0}, groupId={0}", clientId, groupId);
					entryDefinitions.setApplication(application);
					logger.info("Setting the application", ctx.interceptorInstance, application);
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
	public static void after(@Advice.This Consumer<?, ?> producer, //
			@Advice.Origin Method method, //
			@Advice.Thrown Throwable exception, @Advice.Local("ed") EntryDefinition ed,
			@Advice.Local("context") InterceptionContext ctx, //
			@Advice.Local("startTime") long startTime) {
		boolean doFinally = true;
		try {
			ctx = prepareIntercept(KafkaConsumerClientAdvice.class, producer, method);
			if (!ctx.intercept) {
				return;
			}
			doFinally = checkEntryDefinition(ed, ctx);
			fillDefaultValuesAfter(ed, startTime, exception, ctx);
		} catch (Throwable t) {
			handleAdviceException(t, ctx);
		} finally {
			if (doFinally) {
				doFinally(ctx, producer.getClass());
			}
		}
	}

	/**
	 * Type matcher should find the class intended for instrumentation See (@ElementMatcher) for available matches.
	 */

	@Override
	public ElementMatcher<TypeDescription> getTypeMatcher() {
		return hasSuperType(named(INTERCEPTING_CLASS[0])).and(not(isInterface()));
	}

	@Override
	public AgentBuilder.Transformer getAdvice() {
		return advice;
	}

	@Override
	public String getName() {
		return ADVICE_NAME;
	}

}
