package com.jkoolcloud.remora.advices;

import static net.bytebuddy.matcher.ElementMatchers.named;

import java.lang.reflect.Method;
import java.util.logging.Logger;

import com.jkoolcloud.remora.RemoraConfig;
import com.jkoolcloud.remora.core.EntryDefinition;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.NamedElement;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

public class SimpleTest extends BaseTransformers {

	private static final String ADVICE_NAME = "SimpleTest";
	public static String[] INTERCEPTING_CLASS = { "lt.slabs.com.jkoolcloud.remora.JustATest" };
	public static String INTERCEPTING_METHOD = "instrumentedMethod";

	@RemoraConfig.Configurable
	public static boolean logging = true;
	public static Logger logger;
	static {
		logger = Logger.getLogger(SimpleTest.class.getName());
		configureAdviceLogger(logger);
	}

	static AgentBuilder.Transformer.ForAdvice advice = new AgentBuilder.Transformer.ForAdvice()
			.include(SimpleTest.class.getClassLoader()).include(RemoraConfig.INSTANCE.classLoader)

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
			System.out.println("BEFORE METHOD CALL");
			if (ed == null) {
				ed = new EntryDefinition(SimpleTest.class);
				System.out.println("NEW entry def");
			}

			starttime = fillDefaultValuesBefore(ed, stackThreadLocal, thiz, method, logger);
			ed.addProperty("URI", uri.toString());
			ed.addProperty("Arg", arguments.toString());

		} catch (Throwable t) {
			// handleAdviceException(t, ADVICE_NAME, logger);
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
			// fillDefaultValuesAfter(ed, starttime, exception, logger);
		} finally {
			doFinally();
		}

	}

	@Override
	public AgentBuilder.Listener getListener() {
		return new BaseTransformers.TransformationLoggingListener(logger);
	}
}
