package com.jkoolcloud.remora.advices;

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

public class SimpleTest extends BaseTransformers {

	public static final String ADVICE_NAME = "SimpleTest";
	public static String[] INTERCEPTING_CLASS = { "lt.slabs.com.jkoolcloud.remora.JustATest" };
	public static String INTERCEPTING_METHOD = "instrumentedMethod";

	@RemoraConfig.Configurable
	public static boolean load = true;
	@RemoraConfig.Configurable
	public static boolean logging = false;
	public static TaggedLogger logger;

	static AgentBuilder.Transformer.ForAdvice advice = new AgentBuilder.Transformer.ForAdvice()
			.include(SimpleTest.class.getClassLoader())//
			.include(RemoraConfig.INSTANCE.classLoader)//

			.advice(methodMatcher(), "com.jkoolcloud.remora.advices.SimpleTest");

	private static ElementMatcher.Junction<NamedElement> methodMatcher() {
		return named(INTERCEPTING_METHOD);
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
	public static void before(@Advice.This Object thiz, //
			@Advice.Argument(0) Object uri, //
			@Advice.Argument(1) Object arguments, //
			@Advice.Origin Method method, //
			@Advice.Local("ed") EntryDefinition ed, //
			@Advice.Local("starttime") long starttime) //
	{
		try {
			logger.info("BEFORE METHOD CALL");
			System.out.println("BEFORE METHOD CALL");
			ed = getEntryDefinition(ed, SimpleTest.class, logging ? logger : null);
			;

			starttime = fillDefaultValuesBefore(ed, stackThreadLocal, thiz, method, logging ? logger : null);
			ed.addProperty("URI", uri.toString());
			ed.addProperty("Arg", arguments.toString());

		} catch (Throwable t) {
			// handleAdviceException(t, ADVICE_NAME, logging ? logger : null );
		}
	}

	@Advice.OnMethodExit(onThrowable = Throwable.class)
	public static void after(@Advice.This Object obj, //
			@Advice.Origin Method method, //
			@Advice.Thrown Throwable exception, //
			@Advice.Local("ed") EntryDefinition ed, //
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
		return new BaseTransformers.TransformationLoggingListener(logger);
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
