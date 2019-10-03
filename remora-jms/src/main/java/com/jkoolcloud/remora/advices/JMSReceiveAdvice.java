package com.jkoolcloud.remora.advices;

import static net.bytebuddy.matcher.ElementMatchers.hasSuperType;
import static net.bytebuddy.matcher.ElementMatchers.named;

import java.lang.reflect.Method;

import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.QueueReceiver;

import com.jkoolcloud.remora.core.EntryDefinition;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.NamedElement;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

public class JMSReceiveAdvice extends BaseTransformers implements RemoraAdvice {
	private static final String ADVICE_NAME = "JMSReceiveAdvice";
	public static String[] INTERCEPTING_CLASS = { "javax.jms.MessageConsumer" };
	public static String INTERCEPTING_METHOD = "receive";

	private static ElementMatcher.Junction<NamedElement> methodMatcher() {
		return named(INTERCEPTING_METHOD);
	}

	static AgentBuilder.Transformer.ForAdvice advice = new AgentBuilder.Transformer.ForAdvice()
			.include(JMSReceiveAdvice.class.getClassLoader())

			.advice(methodMatcher(), JMSReceiveAdvice.class.getName());

	@Override
	public ElementMatcher<TypeDescription> getTypeMatcher() {
		return hasSuperType(named(INTERCEPTING_CLASS[0]));
	}

	@Override
	public AgentBuilder.Transformer getAdvice() {
		return advice;
	}

	@Advice.OnMethodEnter
	public static void before(@Advice.This MessageConsumer thiz, //
			@Advice.AllArguments Object[] arguments, //
			@Advice.Origin Method method, //
			@Advice.Local("ed") EntryDefinition ed, //
			@Advice.Local("starttime") long starttime) //
	{
		try {
			System.out.println("JR");
			if (isChainedClassInterception(JMSReceiveAdvice.class)) {
				return; // return if its chain of same
			}
			try {
				if (JMSReceiveAdvice.class.equals(stackThreadLocal.get().peek().getAdviceClass())) {
					System.out.println("Stack contains the same advice");
					return; // return if its chain of same
				}
			} catch (Exception e) {
				System.out.println("cant check");
			}

			if (ed == null) {
				ed = new EntryDefinition(JMSReceiveAdvice.class);
			}

			ed.setEventType(EntryDefinition.EventType.RECEIVE);
			starttime = fillDefaultValuesBefore(ed, stackThreadLocal, thiz, method);

			if (thiz instanceof QueueReceiver) {
				ed.addPropertyIfExist("QUEUE", ((QueueReceiver) thiz).getQueue().getQueueName());
			}

		} catch (Throwable t) {
			handleAdviceException(t, ADVICE_NAME);
		}
	}

	@Advice.OnMethodExit(onThrowable = Throwable.class)
	public static void after(@Advice.This MessageConsumer obj, //
			@Advice.Origin Method method, //
			@Advice.AllArguments Object[] arguments, //
			@Advice.Thrown Throwable exception, //
			@Advice.Return Message message, //
			@Advice.Local("ed") EntryDefinition ed, //
			@Advice.Local("starttime") long starttime) {
		boolean doFinnaly = true;
		try {
			if (ed == null) { // ed expected to be null if not created by entry, that's for duplicates
				System.out.println("EntryDefinition not exist");
				doFinnaly = false;
				return;
			}
			if (message != null) {
				ed.addPropertyIfExist("MESSAGE_ID", message.getJMSMessageID());
				ed.addPropertyIfExist("CORR_ID", message.getJMSCorrelationID());
				ed.addPropertyIfExist("TYPE", message.getJMSType());

			}
			System.out.println("JRE");
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
