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

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.websocket.MessageHandler;
import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;

import org.tinylog.TaggedLogger;

import com.jkoolcloud.remora.RemoraConfig;
import com.jkoolcloud.remora.core.EntryDefinition;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

@TransparentAdvice
public class WebsocketSessionAdvice extends BaseTransformers implements RemoraAdvice {

	public static final String ADVICE_NAME = "WebsocketSessionAdvice";
	public static String[] INTERCEPTING_CLASS = { "javax.websocket.Session" };
	public static String INTERCEPTING_METHOD = "addMessageHandler";

	public static Map<MessageHandler, Session> sessionHandlers = new HashMap<>();
	public static Map<RemoteEndpoint, Session> sessionEndpoints = new HashMap<>();

	/**
	 * Method matcher intended to match intercepted class method/s to instrument. See (@ElementMatcher) for available
	 * method matches.
	 */

	private static ElementMatcher<? super MethodDescription> methodMatcher() {
		return named("addMessageHandler");
	}

	/**
	 * Type matcher should find the class intended for instrumentation See (@ElementMatcher) for available matches.
	 */

	@Override
	public ElementMatcher<TypeDescription> getTypeMatcher() {
		return hasSuperType(nameStartsWith(INTERCEPTING_CLASS[0])).and(not(isInterface()));
	}

	@Override
	public AgentBuilder.Transformer getAdvice() {
		return advice;
	}

	static AgentBuilder.Transformer.ForAdvice advice = new AgentBuilder.Transformer.ForAdvice()
			.include(WebsocketSessionAdvice.class.getClassLoader()).include(RemoraConfig.INSTANCE.classLoader)//
			.advice(methodMatcher(), WebsocketSessionAdvice.class.getName());

	/**
	 * Advices before method is called before instrumented method code
	 *
	 * @param thiz
	 *            reference to method object*
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
	public static void before(@Advice.This Session thiz, //
			@Advice.AllArguments Object[] args, //
			@Advice.Origin Method method, //
			@Advice.Local("ed") EntryDefinition ed, @Advice.Local("context") InterceptionContext ctx, //
			@Advice.Local("startTime") long startTime) {
		try {
			ctx = prepareIntercept(WebsocketSessionAdvice.class, thiz, method, args);
			if (!ctx.intercept) {
				return;
			}
			TaggedLogger logger = ctx.interceptorInstance.getLogger();
			MessageHandler handler = null;
			if (args != null && args.length == 1 && args[0] instanceof MessageHandler) {
				handler = (MessageHandler) args[0];
			}
			if (args != null && args.length == 2 && args[1] instanceof MessageHandler) {
				handler = (MessageHandler) args[1];
			}
			logger.info("Found new Handler {} - session {}", handler, ctx.interceptorInstance, thiz);
			sessionHandlers.put(handler, thiz);
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
	 */

	@Advice.OnMethodExit(onThrowable = Throwable.class)
	public static void after(@Advice.This Object obj, @Advice.Local("context") InterceptionContext ctx, //
			@Advice.Origin Method method) {
		ctx = prepareIntercept(WebsocketSessionAdvice.class, obj, method);
		if (!ctx.intercept) {
			return;
		}
	}

	@Override
	public String getName() {
		return ADVICE_NAME;
	}

}
