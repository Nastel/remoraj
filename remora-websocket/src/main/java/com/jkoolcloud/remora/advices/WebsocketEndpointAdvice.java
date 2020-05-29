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

import javax.websocket.CloseReason;
import javax.websocket.MessageHandler;
import javax.websocket.Session;

import org.tinylog.Logger;
import org.tinylog.TaggedLogger;

import com.jkoolcloud.remora.RemoraConfig;
import com.jkoolcloud.remora.core.EntryDefinition;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

public class WebsocketEndpointAdvice extends BaseTransformers implements RemoraAdvice {

	public static final String ADVICE_NAME = "WebsocketEndpointAdvice";
	public static String[] INTERCEPTING_CLASS = { "javax.websocket.Endpoint" };
	public static String INTERCEPTING_METHOD = "onClose,onOpen,onError";

	@RemoraConfig.Configurable
	public static boolean logging = false;
	public static TaggedLogger logger;
	static AgentBuilder.Transformer.ForAdvice advice = new AgentBuilder.Transformer.ForAdvice()
			.include(WebsocketEndpointAdvice.class.getClassLoader()).include(RemoraConfig.INSTANCE.classLoader)//
			.advice(methodMatcher(), WebsocketEndpointAdvice.class.getName());

	/**
	 * Method matcher intended to match intercepted class method/s to instrument. See (@ElementMatcher) for available
	 * method matches.
	 */

	private static ElementMatcher<? super MethodDescription> methodMatcher() {
		return (nameStartsWith("on").or(isAnnotatedWith(nameStartsWith("javax.websocket.On"))))//
				.and(takesArgument(0, named("javax.websocket.Session")));
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
	public static void before(@Advice.This Object thiz, //
			@Advice.AllArguments Object[] args, //
			@Advice.Origin Method method, //
			@Advice.Local("ed") EntryDefinition ed, //
			@Advice.Local("startTime") long startTime) {
		try {
			if (!intercept(WebsocketEndpointAdvice.class, thiz, method, args)) {
				return;
			}
			ed = getEntryDefinition(ed, WebsocketEndpointAdvice.class, logging ? logger : null);
			if (logging) {
				logger.info("Entering: {} {}", WebsocketEndpointAdvice.class.getName(), "before");
			}
			startTime = fillDefaultValuesBefore(ed, stackThreadLocal, thiz, method, logging ? logger : null);

			if (args != null && args.length >= 1 && args[0] instanceof Session) {
				Session session = (Session) args[0];

				String name = method.getName();
				if ("onOpen".equals(name)) {
					if (logging) {
						logger.info("Encountered WebSocket onOpen");
					}
					ed.setEventType(EntryDefinition.EventType.OPEN);
					for (MessageHandler handler : session.getMessageHandlers()) {

						WebsocketSessionAdvice.sessionHandlers.put(handler, session);
						if (logging) {
							logger.info("Adding known handler {} for session {}", handler, session);
						}
					}
					WebsocketSessionAdvice.sessionEndpoints.put(session.getBasicRemote(), session);
					WebsocketSessionAdvice.sessionEndpoints.put(session.getAsyncRemote(), session);
				}
				if ("onClose".equals(name)) {
					if (logging) {
						logger.info("Encountered WebSocket onclose");
					}
					ed.setEventType(EntryDefinition.EventType.CLOSE);
					if (args.length >= 2 && args[1] instanceof CloseReason) {
						ed.addPropertyIfExist("CLOSE_REASON", ((CloseReason) args[1]).getReasonPhrase());
						ed.addPropertyIfExist("CLOSE_CODE", ((CloseReason) args[1]).getCloseCode().getCode());
					}
					WebsocketSessionAdvice.sessionHandlers.keySet().removeAll(session.getMessageHandlers());
					WebsocketSessionAdvice.sessionEndpoints.remove(session.getBasicRemote());
					WebsocketSessionAdvice.sessionEndpoints.remove(session.getAsyncRemote());

				}
				if ("OnError".equals(name)) {
					if (logging) {
						logger.info("Encountered WebSocket onError");
					}
					ed.setEventType(EntryDefinition.EventType.CLOSE);
					if (args.length >= 2 && args[1] instanceof Throwable) {
						if (args[1] instanceof Throwable) {
							ed.setException(((Throwable) args[1]));

						}
					}
					WebsocketSessionAdvice.sessionHandlers.keySet().removeAll(session.getMessageHandlers());
					WebsocketSessionAdvice.sessionEndpoints.remove(session.getBasicRemote());
					WebsocketSessionAdvice.sessionEndpoints.remove(session.getAsyncRemote());

				}
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
			@Advice.Thrown Throwable exception, //
			@Advice.Local("ed") EntryDefinition ed, //
			@Advice.Local("startTime") long startTime) {
		boolean doFinally = true;
		try {
			if (!intercept(WebsocketEndpointAdvice.class, obj, method, arguments)) {
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
				logger.info("Exiting: {} {}", WebsocketEndpointAdvice.class.getName(), "after");
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
		return (hasSuperType(nameStartsWith(INTERCEPTING_CLASS[0]))
				.or(isAnnotatedWith(named("javax.websocket.server.ServerEndpoint")))).and(not(isAbstract()))
						.and(not(isInterface()));
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
			logger.info("Advice {} not enabled", getName());
		}
	}

	@Override
	public String getName() {
		return ADVICE_NAME;
	}

}
