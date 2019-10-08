package com.jkoolcloud.remora.advices;

import static net.bytebuddy.matcher.ElementMatchers.hasSuperType;
import static net.bytebuddy.matcher.ElementMatchers.named;

import java.lang.reflect.Method;
import java.util.logging.Logger;

import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.QueueReceiver;

import com.jkoolcloud.remora.RemoraConfig;
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

	@RemoraConfig.Configurable
	public static boolean logging;
	public static Logger logger = Logger.getLogger(JMSReceiveAdvice.class.getName());

	/**
	 * Method matcher intended to match intercepted class method/s to instrument. See (@ElementMatcher) for available
	 * method matches.
	 */

	private static ElementMatcher.Junction<NamedElement> methodMatcher() {
		return named(INTERCEPTING_METHOD);
	}

	static AgentBuilder.Transformer.ForAdvice advice = new AgentBuilder.Transformer.ForAdvice()
			.include(JMSReceiveAdvice.class.getClassLoader())

			.advice(methodMatcher(), JMSReceiveAdvice.class.getName());

	/**
	 * Type matcher should find the class intended for intrumentation See (@ElementMatcher) for available matches.
	 */

	@Override
	public ElementMatcher<TypeDescription> getTypeMatcher() {
		return hasSuperType(named(INTERCEPTING_CLASS[0]));
	}

	@Override
	public AgentBuilder.Transformer getAdvice() {
		return advice;
	}

	/**
	 * Advices before method is called before instrumented method code
	 *
	 * @param thiz
	 *            reference to method object
	 * @param arguments
	 *            arguments provided for method
	 * @param method
	 *            instrumented method description
	 * @param ed
	 *            {@link EntryDefinition} for collecting ant passing values to
	 *            {@link com.jkoolcloud.remora.core.output.OutputManager}
	 * @param startTime
	 *            method startTime
	 *
	 */

	@Advice.OnMethodEnter
	public static void before(@Advice.This MessageConsumer thiz, //
			@Advice.AllArguments Object[] arguments, //
			@Advice.Origin Method method, //
			@Advice.Local("ed") EntryDefinition ed, //
			@Advice.Local("starttime") long startTime) //
	{
		try {
			if (logging) {
				logger.entering(JMSReceiveAdvice.class.getName(), "before");
			}
			if (ed == null) {
				ed = new EntryDefinition(JMSSendAdvice.class);
			}
			System.out.println("JR");
			if (isChainedClassInterception(JMSReceiveAdvice.class, logger)) {
				return; // return if its chain of same
			}

			if (ed == null) {
				ed = new EntryDefinition(JMSReceiveAdvice.class);
			}

			ed.setEventType(EntryDefinition.EventType.RECEIVE);
			startTime = fillDefaultValuesBefore(ed, stackThreadLocal, thiz, method, logger);

			if (thiz instanceof QueueReceiver) {
				ed.addPropertyIfExist("QUEUE", ((QueueReceiver) thiz).getQueue().getQueueName());
			}

		} catch (Throwable t) {
			handleAdviceException(t, ADVICE_NAME, logger);
		}
	}

	/**
	 * Method called on instrumented method finished.
	 *
	 * @param obj
	 *            reference to method object
	 * @param method
	 *            instrumented method description
	 * @param arguments
	 *            arguments provided for method
	 * @param exception
	 *            exception thrown in method exit (not caught)
	 * @param ed
	 *            {@link EntryDefinition} passed along the method (from before method)
	 * @param startTime
	 *            startTime passed along the method
	 */

	@Advice.OnMethodExit(onThrowable = Throwable.class)
	public static void after(@Advice.This MessageConsumer obj, //
			@Advice.Origin Method method, //
			@Advice.AllArguments Object[] arguments, //
			@Advice.Thrown Throwable exception, //
			@Advice.Return Message message, //
			@Advice.Local("ed") EntryDefinition ed, //
			@Advice.Local("startTime") long startTime) {
		boolean doFinnaly = true;
		try {
			if (ed == null) { // ed expected to be null if not created by entry, that's for duplicates
				logger.fine("EntryDefinition not exist, entry might be filtered out as duplicate or ran on test");
				doFinnaly = false;
				return;
			}
			if (logging) {
				logger.exiting(JMSReceiveAdvice.class.getName(), "after");
			}
			if (message != null) {
				ed.addPropertyIfExist("MESSAGE_ID", message.getJMSMessageID());
				ed.addPropertyIfExist("CORR_ID", message.getJMSCorrelationID());
				ed.addPropertyIfExist("TYPE", message.getJMSType());

			}
			fillDefaultValuesAfter(ed, startTime, exception, logger);
		} catch (Throwable t) {
			handleAdviceException(t, ADVICE_NAME, logger);
		} finally {
			if (doFinnaly) {
				doFinally();
			}
		}

	}

}
