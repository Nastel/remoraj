package com.jkoolcloud.remora.advices;

import static com.jkoolcloud.remora.core.utils.ReflectionUtils.getFieldValue;
import static net.bytebuddy.matcher.ElementMatchers.hasSuperType;
import static net.bytebuddy.matcher.ElementMatchers.named;

import java.lang.reflect.Method;
import java.util.Properties;

import javax.jms.ConnectionFactory;

import com.jkoolcloud.remora.core.EntryDefinition;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.NamedElement;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

public class JMSCreateConnectionAdvice extends BaseTransformers implements RemoraAdvice {

	private static final String ADVICE_NAME = "JMSCreateConnectionAdvice";
	public static String[] INTERCEPTING_CLASS = { "javax.jms.ConnectionFactory" };
	public static String INTERCEPTING_METHOD = "createConnection";

	private static ElementMatcher.Junction<NamedElement> methodMatcher() {
		return named(INTERCEPTING_METHOD).or(named("createQueueConnection"));
	}

	static AgentBuilder.Transformer.ForAdvice advice = new AgentBuilder.Transformer.ForAdvice()
			.include(JMSCreateConnectionAdvice.class.getClassLoader())

			.advice(methodMatcher(), JMSCreateConnectionAdvice.class.getName());

	@Override
	public ElementMatcher<TypeDescription> getTypeMatcher() {
		return hasSuperType(named(INTERCEPTING_CLASS[0]));
	}

	@Override
	public AgentBuilder.Transformer getAdvice() {
		return advice;
	}

	@Advice.OnMethodEnter
	public static void before(@Advice.This ConnectionFactory thiz, //
			@Advice.AllArguments Object[] arguments, //
			@Advice.Origin Method method, //
			@Advice.Local("ed") EntryDefinition ed, //
			@Advice.Local("starttime") long starttime) //
	{
		try {
			System.out.println("JC");
			if (isChainedClassInterception(JMSCreateConnectionAdvice.class)) {
				return; // return if its chain of same
			}

			if (ed == null) {
				ed = new EntryDefinition(JMSCreateConnectionAdvice.class);
			}
			ed.setEventType(EntryDefinition.EventType.OPEN);
			starttime = fillDefaultValuesBefore(ed, stackThreadLocal, thiz, method);

			Properties fieldValue = getFieldValue("factory.properties", thiz, Properties.class);
			if (fieldValue != null) {
				ed.addProperties(fieldValue);
			}

		} catch (Throwable t) {
			System.out.println("###################### Advice Error");
			t.printStackTrace();
		}
	}

	@Advice.OnMethodExit(onThrowable = Throwable.class)
	public static void after(@Advice.This ConnectionFactory obj, //
			@Advice.Origin Method method, //
			@Advice.AllArguments Object[] arguments, //
			@Advice.Thrown Throwable exception, //
			@Advice.Local("ed") EntryDefinition ed, //
			@Advice.Local("starttime") long starttime) {
		boolean doFinnaly = true;
		try {
			if (ed == null) { // ed expected to be null if not created by entry, that's for duplicates
				System.out.println("EntryDefinition not exist");
				doFinnaly = false;
				return;
			}
			System.out.println("JCE");
			fillDefaultValuesAfter(ed, starttime, exception);
		} catch (Throwable t) {
			handleAdviceException(t, ADVICE_NAME);
		} finally {
			if (doFinnaly) {
				doFinally();
			}
		}

	}

}
