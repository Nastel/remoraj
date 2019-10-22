package com.jkoolcloud.remora.advices;

import static com.jkoolcloud.remora.core.utils.ReflectionUtils.getFieldValue;
import static net.bytebuddy.matcher.ElementMatchers.hasSuperType;
import static net.bytebuddy.matcher.ElementMatchers.named;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.util.Properties;

import javax.jms.ConnectionFactory;

import org.tinylog.Logger;
import org.tinylog.TaggedLogger;

import com.jkoolcloud.remora.RemoraConfig;
import com.jkoolcloud.remora.core.EntryDefinition;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.NamedElement;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

public class JMSCreateConnectionAdvice extends BaseTransformers implements RemoraAdvice {

	public static final String ADVICE_NAME = "JMSCreateConnectionAdvice";
	public static String[] INTERCEPTING_CLASS = { "javax.jms.ConnectionFactory" };
	public static String INTERCEPTING_METHOD = "createConnection";

	@RemoraConfig.Configurable
	public static boolean logging = true;
	public static TaggedLogger logger;

	/**
	 * Method matcher intended to match intercepted class method/s to instrument. See (@ElementMatcher) for available
	 * method maches.
	 */

	private static ElementMatcher.Junction<NamedElement> methodMatcher() {
		return named(INTERCEPTING_METHOD).or(named("createQueueConnection"));
	}

	static AgentBuilder.Transformer.ForAdvice advice = new AgentBuilder.Transformer.ForAdvice()
			.include(JMSCreateConnectionAdvice.class.getClassLoader())

			.advice(methodMatcher(), JMSCreateConnectionAdvice.class.getName());

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
	public static void before(@Advice.This ConnectionFactory thiz, //
			@Advice.AllArguments Object[] arguments, //
			@Advice.Origin Method method, //
			@Advice.Local("ed") EntryDefinition ed, //
			@Advice.Local("startTime") long startTime) //
	// @Advice.Local("remoraLogger") Logger logger) //
	{
		try {
			if (logging) {
				logger.info(format("Entering: {0} {1}", JMSCreateConnectionAdvice.class.getName(), "before"));
			}

			if (isChainedClassInterception(JMSCreateConnectionAdvice.class, logger)) {
				return; // return if its chain of same
			}

			if (ed == null) {
				ed = new EntryDefinition(JMSCreateConnectionAdvice.class);
			}
			ed.setEventType(EntryDefinition.EventType.OPEN);

			startTime = fillDefaultValuesBefore(ed, stackThreadLocal, thiz, method, logger);

			Properties fieldValue = getFieldValue("factory.properties", thiz, Properties.class);
			if (fieldValue != null) {
				ed.addProperties(fieldValue);
			}

		} catch (Throwable t) {
			if (logging) {
				logger.info(
						format("Exception: {0} {1} \n {2}", JMSCreateConnectionAdvice.class.getName(), "before", t));
			}
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
	public static void after(@Advice.This ConnectionFactory obj, //
			@Advice.Origin Method method, //
			@Advice.AllArguments Object[] arguments, //
			@Advice.Thrown Throwable exception, //
			@Advice.Local("ed") EntryDefinition ed, //
			@Advice.Local("startTime") long startTime)//
	// @Advice.Local("remoraLogger") Logger logger)
	{
		boolean doFinally = true;
		try {
			if (ed == null) { // ed expected to be null if not created by entry, that's for duplicates
				if (logging) {
					logger.info("EntryDefinition not exist, entry might be filtered out as duplicate or ran on test");
				}
				doFinally = false;
				return;
			}
			if (logging) {
				logger.info(format("Exiting: {0} {1}", JMSCreateConnectionAdvice.class.getName(), "after"));
			}
			fillDefaultValuesAfter(ed, startTime, exception, logger);
		} catch (Throwable t) {
			handleAdviceException(t, ADVICE_NAME, logger);
		} finally {
			if (doFinally) {
				doFinally();
			}
		}

	}

	@Override
	protected AgentBuilder.Listener getListener() {
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
