package com.jkoolcloud.remora.advices;

import static net.bytebuddy.matcher.ElementMatchers.*;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

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

@TransparentAdvice
public class WebsocketSessionAdvice extends BaseTransformers implements RemoraAdvice {

	public static final String ADVICE_NAME = "WebsocketSessionAdvice";
	public static String[] INTERCEPTING_CLASS = { "javax.websocket.Session" };
	public static String INTERCEPTING_METHOD = "addMessageHandler";

	public static Map<MessageHandler, String> sessionHandlers = new HashMap<>();

	@RemoraConfig.Configurable
	public static boolean logging = true;
	public static TaggedLogger logger;

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
			@Advice.Argument(0) Object arg1handler, //
			@Advice.Argument(0) Object arg2Handler, //
			@Advice.Origin Method method, //
			@Advice.Local("ed") EntryDefinition ed, //
			@Advice.Local("startTime") long startTime) {
		try {
			MessageHandler handler = null;
			if (arg1handler instanceof MessageHandler) {
				handler = (MessageHandler) arg1handler;
			}
			if (arg2Handler instanceof MessageHandler) {
				handler = (MessageHandler) arg2Handler;
			}
			logger.info("Found new Handler {0} - session {1}", handler, thiz);
			sessionHandlers.put(handler, thiz.getId());
		} catch (Throwable t) {
			handleAdviceException(t, ADVICE_NAME, logger);
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
	public static void after(@Advice.This Object obj, //
			@Advice.Origin Method method) {

	}

	@Override
	protected AgentBuilder.Listener getListener() {
		return new TransformationLoggingListener(logger);
	}

	@Override
	public void install(Instrumentation instrumentation) {
		logger = Logger.tag(ADVICE_NAME);
		getTransform().with(getListener()).installOn(instrumentation);
	}

	@Override
	public String getName() {
		return ADVICE_NAME;
	}

}
