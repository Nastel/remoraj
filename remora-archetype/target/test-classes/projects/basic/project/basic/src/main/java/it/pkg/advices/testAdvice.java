package it.pkg.advices;

import java.lang.reflect.Method;

import com.jkoolcloud.remora.advices.BaseTranformers;
import com.jkoolcloud.remora.advices.GeneralAdvice;
import com.jkoolcloud.remora.core.EntryDefinition;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;

public class testAdvice extends BaseTranformers implements RemoraAdvice {


	private static final String ADVICE_NAME = "testAdvice";

	@Advice.OnMethodEnter
	public static void before(@Advice.This Object thiz, //
			@Advice.AllArguments Object[] arguments, //
			@Advice.Origin Method method, //
			@Advice.Local("ed") EntryDefinition ed, //
			@Advice.Local("starttime") long starttime) {
		try {
			starttime = fillDefaultValuesBefore(ed, stackThreadLocal, thiz, method);
		} catch (Throwable t) {
			handleAdviceException(t, ADVICE_NAME);
		}
	}

	@Advice.OnMethodExit(onThrowable = Throwable.class)
	public static void after(@Advice.This Object obj, //
			@Advice.Origin Method method, //
			// @Advice.Return Object returnValue, // //TODO needs separate Advice capture for void type
			@Advice.Thrown Throwable exception, @Advice.Local("ed") EntryDefinition ed, //
			@Advice.Local("starttime") long starttime) {
		try {
			fillDefaultValuesAfter(ed, starttime, exception);
		} finally {
			doFinally();
		}

	}

	@Override
	public EnhancedElementMatcher<TypeDescription> getTypeMatcher() {
		return null;
	}

	@Override
	public AgentBuilder.Transformer getAdvice() {
		return null;
	}

    @Override
    public void install(Instrumentation instrumentation) {
        getTransform().installOn(instrumentation);
	}
}