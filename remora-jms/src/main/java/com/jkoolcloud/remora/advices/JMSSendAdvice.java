package com.jkoolcloud.remora.advices;

import static net.bytebuddy.matcher.ElementMatchers.hasSuperType;
import static net.bytebuddy.matcher.ElementMatchers.named;

import java.lang.reflect.Method;

import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.QueueSender;

import com.jkoolcloud.remora.core.EntryDefinition;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.NamedElement;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

public class JMSSendAdvice extends BaseTransformers implements RemoraAdvice {

	private static final String ADVICE_NAME = "JMSSendAdvice";
	public static String[] INTERCEPTING_CLASS = { "javax.jms.MessageProducer" };
	public static String INTERCEPTING_METHOD = "send";

	private static ElementMatcher.Junction<NamedElement> methodMatcher() {
		return named(INTERCEPTING_METHOD);
	}

	static AgentBuilder.Transformer.ForAdvice advice = new AgentBuilder.Transformer.ForAdvice()
			.include(JMSSendAdvice.class.getClassLoader()).advice(methodMatcher(), JMSSendAdvice.class.getName());

	@Override
	public ElementMatcher<TypeDescription> getTypeMatcher() {
		return hasSuperType(named(INTERCEPTING_CLASS[0]));
	}

	@Override
	public AgentBuilder.Transformer getAdvice() {
		return advice;
	}

	@Advice.OnMethodEnter
	public static void before(@Advice.This MessageProducer thiz, //
			@Advice.AllArguments Object[] arguments, //
			@Advice.Origin Method method, //
			@Advice.Local("ed") EntryDefinition ed, //
			@Advice.Local("starttime") long starttime) //
	{
		try {
			System.out.println("J");
			if (isChainedClassInterception(JMSSendAdvice.class)) {
				return; // return if its chain of same
			}

			if (ed == null) {
				ed = new EntryDefinition(JMSSendAdvice.class);
			}
			ed.setEventType(EntryDefinition.EventType.SEND);
			starttime = fillDefaultValuesBefore(ed, stackThreadLocal, thiz, method);

			if (thiz instanceof QueueSender) {
				ed.addPropertyIfExist("QUEUE", ((QueueSender) thiz).getQueue().getQueueName());
			}

			for (Object argument : arguments) {
				if (argument instanceof Queue) {
					Queue destination = (Queue) argument;
					ed.addPropertyIfExist("QUEUE", destination.getQueueName());
				}
				if (argument instanceof Message) {
					Message message = (Message) argument;
					ed.addPropertyIfExist("MESSAGE_ID", message.getJMSMessageID());
					ed.addPropertyIfExist("CORR_ID", message.getJMSCorrelationID());
					ed.addPropertyIfExist("TYPE", message.getJMSType());
					try {
						message.setObjectProperty("JanusMessageSignature", ed.getCorrelator());
					} catch (Exception e) {
						System.out.println("Cannot alter message");
					}
				}

			}

		} catch (Throwable t) {
			System.out.println("###################### Advice Error");
			t.printStackTrace();
		}
	}

	@Advice.OnMethodExit(onThrowable = Throwable.class)
	public static void after(@Advice.This MessageProducer obj, //
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
			System.out.println("JE");
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
