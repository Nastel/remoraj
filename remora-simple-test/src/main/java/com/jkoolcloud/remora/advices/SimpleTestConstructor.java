package com.jkoolcloud.remora.advices;

import static net.bytebuddy.matcher.ElementMatchers.isConstructor;
import static net.bytebuddy.matcher.ElementMatchers.named;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.util.Stack;

import org.tinylog.Logger;
import org.tinylog.TaggedLogger;

import com.jkoolcloud.remora.RemoraConfig;
import com.jkoolcloud.remora.core.CallStack;
import com.jkoolcloud.remora.core.EntryDefinition;
import com.jkoolcloud.remora.core.output.OutputManager;

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

	public static ThreadLocal<CallStack<EntryDefinition>> stackThreadLocal = new ThreadLocal<>();

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
			System.out.println("BEFORE METHOD CALL");
			ed = new EntryDefinition(SimpleTestConstructor.class);
			System.out.println("NEW entry def");
			logger.info("NEW entry def");

			starttime = fillDefaultValuesBefore(ed, stackThreadLocal, null, null, logging ? logger : null);

		} catch (Throwable t) {
			// handleAdviceException(t, ADVICE_NAME, logging ? logger : null );
		}
	}

	public static void after(@Advice.Local("ed") EntryDefinition ed, //
			@Advice.Local("starttime") long starttime) {
	}

	public static long fillDefaultValuesBefor(EntryDefinition entryDefinition,
			ThreadLocal<Stack<EntryDefinition>> stackThreadLocal, Object thiz, Method method, TaggedLogger logger) {
		System.out.println("default values");
		try {
			if (thiz != null) {
				entryDefinition.setClazz(thiz.getClass().getName());
			} else {
				if (logger != null) {
					logger.info("This not filled");
				}
			}

			if (method != null) {
				// entryDefinition.setName(method.getName());
			} else {
				if (logger != null) {
					logger.info("#Method not filled");
				}
			}

			if (stackThreadLocal != null && stackThreadLocal.get() == null) {
				Stack<EntryDefinition> definitions = new Stack<>();
				stackThreadLocal.set(definitions);
			}

			stackThreadLocal.get().push(entryDefinition);
			entryDefinition.setThread(Thread.currentThread().toString());
			entryDefinition.setStartTime(System.currentTimeMillis());
			if (sendStackTrace) {
				entryDefinition.setStackTrace(getStackTrace());
				OutputManager.INSTANCE.send(entryDefinition);
			}
		} catch (Throwable t) {
			if (logger != null) {
				logger.info(format("####Advice error/fillDefaultValuesBefore: {0}", t));
			}
		}
		return System.nanoTime();
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
