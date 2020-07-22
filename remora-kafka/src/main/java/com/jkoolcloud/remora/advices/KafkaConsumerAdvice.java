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

import static net.bytebuddy.matcher.ElementMatchers.isConstructor;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.takesArgument;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.Stack;

import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.record.TimestampType;
import org.tinylog.Logger;

import com.jkoolcloud.remora.RemoraConfig;
import com.jkoolcloud.remora.core.EntryDefinition;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

public class KafkaConsumerAdvice extends BaseTransformers implements RemoraAdvice {

	public static final String ADVICE_NAME = "KafkaConsumerAdvice";
	public static final String HEADER_PREFIX = "HDR_";
	public static String[] INTERCEPTING_CLASS = { "org.apache.kafka.clients.consumer.ConsumerRecord" };
	public static String INTERCEPTING_METHOD = "ConsumerRecord";

	public static ThreadLocal<Stack<Long>> startTimeThreadLocal = new ThreadLocal<>();
	static AgentBuilder.Transformer.ForAdvice advice = new AgentBuilder.Transformer.ForAdvice()
			.include(KafkaConsumerAdvice.class.getClassLoader()).include(RemoraConfig.INSTANCE.classLoader)//
			.advice(methodMatcher(), KafkaConsumerAdvice.class.getName());

	/**
	 * Method matcher intended to match intercepted class method/s to instrument. See (@ElementMatcher) for available
	 * method matches.
	 */

	private static ElementMatcher<? super MethodDescription> methodMatcher() {
		return isConstructor().and(takesArgument(0, String.class))// topic
				.and(takesArgument(1, int.class))// partition
				.and(takesArgument(2, long.class))// offset
				.and(takesArgument(3, long.class))// timestamp
				.and(takesArgument(4, named("org.apache.kafka.common.record.TimestampType")))// timestampType
				.and(takesArgument(5, Long.class))// checksum
				.and(takesArgument(6, int.class))// serializedKeySize
				.and(takesArgument(7, int.class))// serializedValueSize
				.and(takesArgument(8, Object.class))// key
				.and(takesArgument(9, Object.class))// value
				.and(takesArgument(10, named("org.apache.kafka.common.header.Headers")))// headers
				.and(takesArgument(11, Optional.class)// leaderEpoch
				);
	}

	/**
	 * Advices before method is called before instrumented method code
	 *
	 */

	@Advice.OnMethodEnter
	public static void before(@Advice.This Object thiz, //
			@Advice.Origin Method method, //
			@Advice.AllArguments Object[] arguments, //
			@Advice.Argument(0) String topic, //
			@Advice.Argument(1) int partition, //
			@Advice.Argument(2) long offset, //
			@Advice.Argument(3) long timestamp, //
			@Advice.Argument(4) TimestampType timestampType, //
			@Advice.Argument(5) Long checksum, //
			@Advice.Argument(6) int serializedKeySize, //
			@Advice.Argument(7) int serializedValueSize, //
			@Advice.Argument(8) Object key, //
			@Advice.Argument(9) Object value, //
			@Advice.Argument(10) Headers headers, //
			@Advice.Local("ed") EntryDefinition ed, @Advice.Local("context") InterceptionContext ctx, //
			@Advice.Local("startTime") long startTime)
	//
	{
		try {
			ctx = prepareIntercept(KafkaConsumerAdvice.class, thiz, method, arguments);
			if (!ctx.intercept) {
				return;
			}
			ed = getEntryDefinition(ed, KafkaConsumerAdvice.class, ctx);
			ed.setName("consume");
			ed.setEventType(EntryDefinition.EventType.RECEIVE);
			ed.addPropertyIfExist("TOPIC", topic);
			ed.setResource(topic, EntryDefinition.ResourceType.TOPIC);

			ed.addPropertyIfExist("PARTITION", partition);
			ed.addPropertyIfExist("OFFSET", offset);
			ed.addPropertyIfExist("TIMESTAMP", timestamp);
			ed.addPropertyIfExist("TIMESTAMPTYPE", String.valueOf(timestampType));
			ed.addPropertyIfExist("CHECKSUM", checksum);
			ed.addPropertyIfExist("SERIALIZEDKEYSIZE", serializedKeySize);
			ed.addPropertyIfExist("SERIALIZEDVALUESIZE", serializedValueSize);
			ed.addPropertyIfExist("KEY", String.valueOf(key));
			ed.addPropertyIfExist("VALUE", String.valueOf(value));
			for (Header header : headers) {
				ed.addPropertyIfExist(HEADER_PREFIX + header.key(), String.valueOf(header.value()));
			}
			startTime = fillDefaultValuesBefore(ed, stackThreadLocal, null, null, ctx);//
			ed.setEventType(EntryDefinition.EventType.RECEIVE);
		} catch (Throwable t) {
			handleAdviceException(t, ctx);
		}
	}

	/**
	 * Method called on instrumented method finished.
	 *
	 */

	@Advice.OnMethodExit
	public static void after(@Advice.This Object thiz, //
			@Advice.Origin Method method, //
			@Advice.AllArguments Object[] arguments, //
			@Advice.Local("ed") EntryDefinition ed, @Advice.Local("context") InterceptionContext ctx, //
			@Advice.Local("startTime") long startTime) {
		boolean doFinally = true;

		try {

			ctx = prepareIntercept(KafkaConsumerAdvice.class, thiz, method, arguments);
			if (!ctx.intercept) {
				return;
			}
			doFinally = checkEntryDefinition(ed, ctx);
			fillDefaultValuesAfter(ed, startTime, null, ctx);
		} catch (Throwable t) {
			handleAdviceException(t, ctx);
		} finally {
			if (doFinally) {
				doFinally(ctx, thiz.getClass());
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
	public void install(Instrumentation instrumentation) {
		logger = Logger.tag(ADVICE_NAME);
		if (load) {
			getTransform().with(getListener()).installOn(instrumentation);
		} else {
			logger.info("Advice {} not enabled", this, getName());
		}
	}

	@Override
	public String getName() {
		return ADVICE_NAME;
	}

}
