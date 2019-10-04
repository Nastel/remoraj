package com.jkoolcloud.remora.advices;

import static net.bytebuddy.matcher.ElementMatchers.nameStartsWith;

import java.lang.reflect.Method;
import java.util.Arrays;

import com.ibm.ws.rsadapter.jdbc.WSJdbcStatement;
import com.jkoolcloud.remora.RemoraConfig;
import com.jkoolcloud.remora.core.EntryDefinition;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;

public class IBMAdapterRSA extends BaseTransformers implements RemoraAdvice {

	private static final String ADVICE_NAME = "IBMAdapterRSA";
	public static String[] INTERCEPTING_CLASS = { "com.ibm.ws.rsadapter.jdbc.WSJdbcStatement",
			"com.ibm.ws.rsadapter.jdbc.WSJdbcPreparedStatement", "com.ibm.ws.rsadapter.jdbc.WSJdbcCallableStatement" };
	public static String INTERCEPTING_METHOD = "execut";

	static AgentBuilder.Transformer.ForAdvice advice = new AgentBuilder.Transformer.ForAdvice()
			.include(IBMAdapterRSA.class.getClassLoader()) //
			.include(RemoraConfig.INSTANCE.classLoader) //
			.advice((nameStartsWith("execut")), IBMAdapterRSA.class.getName());

	@Override
	public EnhancedElementMatcher<TypeDescription> getTypeMatcher() {
		return new EnhancedElementMatcher<>(INTERCEPTING_CLASS);
	}

	@Override
	public AgentBuilder.Transformer getAdvice() {
		return advice;
	}

	@Advice.OnMethodEnter
	public static void before(@Advice.This WSJdbcStatement thiz, //
			@Advice.AllArguments Object[] arguments, //
			@Advice.Origin Method method, //
			@Advice.Local("ed") EntryDefinition ed, //
			@Advice.Local("starttime") long starttime

	) {
		try {
			System.out.println("M");
			if (isChainedClassInterception(IBMAdapterRSA.class)) {
				return; // return if its chain of same
			}
			if (ed == null) {
				ed = new EntryDefinition(IBMAdapterRSA.class);
			}
			starttime = fillDefaultValuesBefore(ed, stackThreadLocal, thiz, method);
			if (arguments != null && arguments.length >= 1 && arguments[0] instanceof String) {
				ed.addProperty("SQL", arguments[0].toString());

			} else {
				System.out.println("Augmenting SQL fault" + Arrays.toString(arguments));
			}

			if (thiz != null) {
				ed.addProperty("DB_NAME", thiz.getJNDIName());
			}
		} catch (Throwable t) {
			handleAdviceException(t, ADVICE_NAME);
		}
	}

	@Advice.OnMethodExit(onThrowable = Throwable.class)
	public static void after(@Advice.This Object obj, //
			@Advice.Origin Method method, //
			@Advice.Thrown Throwable exception, @Advice.Local("ed") EntryDefinition ed, //
			@Advice.Local("starttime") long starttime) {
		try {
			System.out.println("ME");
			fillDefaultValuesAfter(ed, starttime, exception);
			ed.addProperty("Return", "true");
		} finally {
			doFinally();
		}

	}
}
