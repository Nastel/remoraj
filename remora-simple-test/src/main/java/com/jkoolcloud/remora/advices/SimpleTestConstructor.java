package com.jkoolcloud.remora.advices;

import static net.bytebuddy.matcher.ElementMatchers.isConstructor;
import static net.bytebuddy.matcher.ElementMatchers.named;

import java.lang.instrument.Instrumentation;

import org.tinylog.Logger;
import org.tinylog.TaggedLogger;

import com.jkoolcloud.remora.RemoraConfig;
import com.jkoolcloud.remora.core.EntryDefinition;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

public class SimpleTestConstructor extends BaseTransformers {

	public static final String ADVICE_NAME = "SimpleTestConstuctor";
	public static String[] INTERCEPTING_CLASS = { "lt.slabs.com.jkoolcloud.remora.JustATest2" };
	public static String INTERCEPTING_METHOD = "constructor";

	@RemoraConfig.Configurable
	public static boolean load = true;
	@RemoraConfig.Configurable
	public static boolean logging = false;
	public static TaggedLogger logger;

	static AgentBuilder.Transformer.ForAdvice advice = new AgentBuilder.Transformer.ForAdvice()
			.include(SimpleTestConstructor.class.getClassLoader())//
			.include(RemoraConfig.INSTANCE.classLoader)//

			.advice(methodMatcher(), "com.jkoolcloud.remora.advices.SimpleTestConstructor");

	private static ElementMatcher.Junction<MethodDescription> methodMatcher() {
		return isConstructor();
	}

	@Override
	public ElementMatcher<TypeDescription> getTypeMatcher() {
		return named(INTERCEPTING_CLASS[0]);
	}

	@Override
	public AgentBuilder.Transformer getAdvice() {
		return advice;
	}

	@Advice.OnMethodEnter
	public static void before(@Advice.Local("ed") EntryDefinition ed, //
			@Advice.Local("starttime") long starttime) //
	{
		try {
			logger.info("BEFORE METHOD CALL");
			System.out.println("BEFORE METHOD CALL");
			if (ed == null) {
				ed = new EntryDefinition(SimpleTestConstructor.class);
				System.out.println("NEW entry def");
				logger.info("NEW entry def");
			}

			starttime = fillDefaultValuesBefore(ed, stackThreadLocal, null, null, logging ? logger : null);

		} catch (Throwable t) {
			// handleAdviceException(t, ADVICE_NAME, logging ? logger : null );
		}
	}

	public static void after(@Advice.Local("ed") EntryDefinition ed, //
			@Advice.Local("starttime") long starttime) {
		try {
			System.out.println("###AFTER METHOD CALL");
			// fillDefaultValuesAfter(ed, startTime, exception, logging ? logger : null );
		} finally {
			doFinally();
		}

	}

	@Override
	public AgentBuilder.Listener getListener() {
		return new TransformationLoggingListener(logger);
	}

	@Override
	public String getName() {
		return ADVICE_NAME;
	}

	@Override
	public void install(Instrumentation inst) {
		logger = Logger.tag(ADVICE_NAME);
		getTransform().with(getListener()).installOn(inst);
	}
}
