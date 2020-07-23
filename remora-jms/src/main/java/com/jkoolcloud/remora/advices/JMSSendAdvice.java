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

import java.lang.reflect.Method;

import javax.jms.*;

import org.tinylog.TaggedLogger;

import com.jkoolcloud.remora.RemoraConfig;
import com.jkoolcloud.remora.core.EntryDefinition;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.NamedElement;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

public class JMSSendAdvice extends BaseTransformers implements RemoraAdvice {

	public static final String ADVICE_NAME = "JMSSendAdvice";
	public static final String[] INTERCEPTING_CLASS = { "javax.jms.MessageProducer" };
	public static final String INTERCEPTING_METHOD = "send";

	@RemoraConfig.Configurable
	public static boolean fetchMsg = false;

	static AgentBuilder.Transformer.ForAdvice advice = new AgentBuilder.Transformer.ForAdvice()
			.include(JMSSendAdvice.class.getClassLoader()) //
			.include(RemoraConfig.INSTANCE.classLoader) //
			.advice(methodMatcher(), JMSSendAdvice.class.getName());

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
	 *
	 */

	@Advice.OnMethodEnter
	public static void before(@Advice.This MessageProducer thiz, //
			@Advice.AllArguments Object[] arguments, //
			@Advice.Origin Method method, //
			@Advice.Local("ed") EntryDefinition ed, @Advice.Local("context") InterceptionContext ctx, //
			@Advice.Local("startTime") long startTime) //
	{
		try {
			ctx = prepareIntercept(JMSSendAdvice.class, thiz, method, arguments);
			if (!ctx.intercept) {
				return;
			}
			TaggedLogger logger = ctx.interceptorInstance.getLogger();

			ed = getEntryDefinition(ed, JMSSendAdvice.class, ctx);
			ed.setEventType(EntryDefinition.EventType.SEND);
			startTime = fillDefaultValuesBefore(ed, stackThreadLocal, thiz, method, ctx);

			if (thiz instanceof QueueSender) {
				String queueName = ((QueueSender) thiz).getQueue().getQueueName();
				ed.addPropertyIfExist("QUEUE", queueName);
				ed.addPropertyIfExist("QUEUE", queueName);
				ed.setResource(queueName, EntryDefinition.ResourceType.QUEUE);
			}

			for (Object argument : arguments) {
				if (argument instanceof Queue) {
					Queue destination = (Queue) argument;
					String queueName = destination.getQueueName();
					ed.addPropertyIfExist("QUEUE", queueName);
					ed.setResource(queueName, EntryDefinition.ResourceType.QUEUE);

				}
				if (argument instanceof Message) {
					Message message = (Message) argument;
					ed.addPropertyIfExist("MESSAGE_ID", message.getJMSMessageID());
					ed.addPropertyIfExist("CORR_ID", message.getJMSCorrelationID());
					ed.addPropertyIfExist("TYPE", message.getJMSType());
					if (fetchMsg && message instanceof TextMessage) {
						ed.addPropertyIfExist("MSG", ((TextMessage) message).getText());
					}
					try {
						message.setObjectProperty("JanusMessageSignature", ed.getCorrelator());
					} catch (Exception e) {
						logger.info("Cannot alter message");
					}
				}

			}

		} catch (Throwable t) {
			handleAdviceException(t, ctx);
		}
	}

	@Advice.OnMethodExit(onThrowable = Throwable.class)
	public static void after(@Advice.This MessageProducer obj, //
			@Advice.Origin Method method, //
			@Advice.AllArguments Object[] arguments, //
			@Advice.Thrown Throwable exception, //
			@Advice.Local("ed") EntryDefinition ed, @Advice.Local("context") InterceptionContext ctx, //
			@Advice.Local("startTime") long startTime //
	// @Advice.Local("remoraLogger") Logger logger//
	) {
		boolean doFinally = true;
		try {
			ctx = prepareIntercept(JMSSendAdvice.class, obj, method, arguments);
			if (!ctx.intercept) {
				return;
			}
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

	@Override
	public String getName() {
		return ADVICE_NAME;
	}

}
