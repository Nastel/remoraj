#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.advices;

import java.lang.reflect.Method;

import com.jkoolcloud.remora.advices.BaseTranformers;
import com.jkoolcloud.remora.advices.GeneralAdvice;
import com.jkoolcloud.remora.core.EntryDefinition;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import java.lang.instrument.Instrumentation;

import static net.bytebuddy.matcher.ElementMatchers.hasSuperType;
import static net.bytebuddy.matcher.ElementMatchers.named;

public class ${adviceClassName}Advice extends BaseTranformers implements RemoraAdvice {


	private static final String ADVICE_NAME = "${adviceClassName}Advice";
	public static String[] INTERCEPTING_CLASS = {"<CHANGE HERE>"};
	public static String INTERCEPTING_METHOD = "<CHANGE HERE>";

	@Override
	public EnhancedElementMatcher<TypeDescription> getTypeMatcher() {
			return hasSuperType(named(INTERCEPTING_CLASS[0]));
	}

	@Override
	public AgentBuilder.Transformer getAdvice() {
		return advice;
	}

	static AgentBuilder.Transformer.ForAdvice advice = new AgentBuilder.Transformer.ForAdvice()
		.include(${adviceClassName}Advice.class.getClassLoader())
		.advice(methodMatcher(), ${adviceClassName}Advice.class.getName());

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
		boolean doFinnaly = true;
		try {
			fillDefaultValuesAfter(ed, starttime, exception);
		} finally {
			if (doFinnaly) {
				doFinally();
			}
		}

	}

}