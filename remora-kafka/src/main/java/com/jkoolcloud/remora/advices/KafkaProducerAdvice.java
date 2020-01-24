package com.jkoolcloud.remora.advices;

import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.takesArgument;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.util.Stack;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.tinylog.Logger;
import org.tinylog.TaggedLogger;

import com.jkoolcloud.remora.RemoraConfig;
import com.jkoolcloud.remora.core.CallStack;
import com.jkoolcloud.remora.core.EntryDefinition;
import com.jkoolcloud.remora.core.utils.ReflectionUtils;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

public class KafkaProducerAdvice extends BaseTransformers implements RemoraAdvice {

	public static final String ADVICE_NAME = "KafkaProducerAdvice";
	public static String[] INTERCEPTING_CLASS = { "org.apache.kafka.clients.producer.KafkaProducer" };
	public static String INTERCEPTING_METHOD = "send";

	@RemoraConfig.Configurable
	public static boolean load = true;
	@RemoraConfig.Configurable
	public static boolean logging = false;
	public static TaggedLogger logger;
	static AgentBuilder.Transformer.ForAdvice advice = new AgentBuilder.Transformer.ForAdvice()
			.include(KafkaProducerAdvice.class.getClassLoader()).include(RemoraConfig.INSTANCE.classLoader)//
			.advice(methodMatcher(), KafkaProducerAdvice.class.getName());

	/**
	 * Method matcher intended to match intercepted class method/s to instrument. See (@ElementMatcher) for available
	 * method matches.
	 */

	private static ElementMatcher<? super MethodDescription> methodMatcher() {
		return named(INTERCEPTING_METHOD)
				.and(takesArgument(0, named("org.apache.kafka.clients.producer.ProducerRecord")));
	}

	/**
	 * Advices before method is called before instrumented method code
	 *
	 * @param thiz
	 *            reference to method object
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
	public static void before(@Advice.This KafkaProducer thiz, //
			@Advice.Argument(0) ProducerRecord record, //
			@Advice.Origin Method method, //
			@Advice.Local("ed") EntryDefinition ed, //
			@Advice.Local("startTime") long startTime) {
		try {

			ed = getEntryDefinition(ed, KafkaProducerAdvice.class, logging ? logger : null);
            if (logging) {
				logger.info(format("Entering: {0} {1}", KafkaProducerAdvice.class.getName(), "before"));
			}

			startTime = fillDefaultValuesBefore(ed, stackThreadLocal, thiz, method, logging ? logger : null);
			ed.setEventType(EntryDefinition.EventType.SEND);
			String topic = record.topic();

			Stack<EntryDefinition> entryDefinitions = stackThreadLocal.get();
			if (entryDefinitions != null) {
				try {
					String application = ReflectionUtils.getFieldValue(thiz, String.class, "clientId");
					((CallStack) entryDefinitions).setApplication(application);
					if (logging) {
						logger.info(format("Setting the application", application));
					}
				} catch (IllegalArgumentException e) {

				}

			}

			ed.addPropertyIfExist("TOPIC", topic);
			ed.addPropertyIfExist("TIMESTAMP", record.timestamp());
			ed.addPropertyIfExist("PARTITION", record.partition());
			ed.addPropertyIfExist("KEY", String.valueOf(record.key()));
			ed.addPropertyIfExist("VALUE", String.valueOf(record.value()));
			ed.setResource(topic, EntryDefinition.ResourceType.TOPIC);
		} catch (Throwable t) {
			handleAdviceException(t, ADVICE_NAME, logging ? logger : null);
		}
	}

	/**
	 * Method called on instrumented method finished.
	 *
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
	public static void after(@Advice.This KafkaProducer producer, //
			@Advice.Origin Method method, //
			@Advice.AllArguments Object[] arguments, //
			@Advice.Thrown Throwable exception, @Advice.Local("ed") EntryDefinition ed, //
			@Advice.Local("startTime") long startTime) {
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
				logger.info(format("Exiting: {0} {1}", KafkaProducerAdvice.class.getName(), "after"));
			}
			fillDefaultValuesAfter(ed, startTime, exception, logging ? logger : null);
		} catch (Throwable t) {
			handleAdviceException(t, ADVICE_NAME, logging ? logger : null);
		} finally {
			if (doFinally) {
				doFinally(logger);
			}
		}

	}

	/**
	 * Type matcher should find the class intended for instrumentation See (@ElementMatcher) for available matches.
	 */

	@Override
	public ElementMatcher<TypeDescription> getTypeMatcher() {
		return named(INTERCEPTING_CLASS[0]);
	}

	@Override
	public AgentBuilder.Transformer getAdvice() {
		return advice;
	}

	@Override
	protected AgentBuilder.Listener getListener() {
		return new TransformationLoggingListener(logger);
	}

	@Override
	public void install(Instrumentation instrumentation) {
		logger = Logger.tag(ADVICE_NAME);
		if (load) {
			getTransform().with(getListener()).installOn(instrumentation);
		} else {
			logger.info("Advice {0} not enabled", getName());
		}
	}

	@Override
	public String getName() {
		return ADVICE_NAME;
	}

}
