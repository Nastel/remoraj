package com.jkoolcloud.remora.advices;

import static net.bytebuddy.matcher.ElementMatchers.nameStartsWith;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.logging.Logger;

import com.ibm.ws.rsadapter.jdbc.WSJdbcStatement;
import com.jkoolcloud.remora.RemoraConfig;
import com.jkoolcloud.remora.core.EntryDefinition;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.NamedElement;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

public class IBMAdapterRSA extends BaseTransformers implements RemoraAdvice {

	private static final String ADVICE_NAME = "IBMAdapterRSA";
	public static String[] INTERCEPTING_CLASS = { "com.ibm.ws.rsadapter.jdbc.WSJdbcStatement",
			"com.ibm.ws.rsadapter.jdbc.WSJdbcPreparedStatement", "com.ibm.ws.rsadapter.jdbc.WSJdbcCallableStatement" };
	public static String INTERCEPTING_METHOD = "execut";

	@RemoraConfig.Configurable
	public static boolean logging = true;
	public static Logger logger;
	static {
		logger = Logger.getLogger(IBMAdapterRSA.class.getName());
		configureAdviceLogger(logger);
	}

	static AgentBuilder.Transformer.ForAdvice advice = new AgentBuilder.Transformer.ForAdvice()
			.include(IBMAdapterRSA.class.getClassLoader()) //
			.include(RemoraConfig.INSTANCE.classLoader) //
			.advice(methodMatcher(), IBMAdapterRSA.class.getName());

	/**
	 * Method matcher intended to match intercepted class method/s to instrument. See (@ElementMatcher) for available
	 * method matches.
	 */

	private static ElementMatcher.Junction<NamedElement> methodMatcher() {
		return (nameStartsWith(INTERCEPTING_METHOD));
	}

	/**
	 * Type matcher should find the class intended for instrumentation See (@ElementMatcher) for available matches.
	 */
	@Override
	public EnhancedElementMatcher<TypeDescription> getTypeMatcher() {
		return new EnhancedElementMatcher<>(INTERCEPTING_CLASS);
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
	public static void before(@Advice.This WSJdbcStatement thiz, //
			@Advice.AllArguments Object[] arguments, //
			@Advice.Origin Method method, //
			@Advice.Local("ed") EntryDefinition ed, //
			@Advice.Local("startTime") long startTime //
	// @Advice.Local("remoraLogger") Logger logger

	) {
		try {
			if (logging) {
				logger = Logger.getLogger(IBMAdapterRSA.class.getName());
				logger.info(format("Entering: {0} {1}", IBMAdapterRSA.class.getName(), "before"));
			}
			if (isChainedClassInterception(IBMAdapterRSA.class, logger)) {
				return; // return if its chain of same
			}
			if (ed == null) {
				ed = new EntryDefinition(IBMAdapterRSA.class);
			}
			startTime = fillDefaultValuesBefore(ed, stackThreadLocal, thiz, method, logger);
			if (arguments != null && arguments.length >= 1 && arguments[0] instanceof String) {
				ed.addProperty("SQL", arguments[0].toString());

			} else {
				logger.info("Augmenting SQL fault" + Arrays.toString(arguments));
			}

			if (thiz != null) {
				ed.addProperty("DB_NAME", thiz.getJNDIName());
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
	public static void after(@Advice.This Object obj, //
			@Advice.Origin Method method, //
			@Advice.AllArguments Object[] arguments, //
			@Advice.Thrown Throwable exception, @Advice.Local("ed") EntryDefinition ed, //
			@Advice.Local("startTime") long startTime //
	// @Advice.Local("remoraLogger") Logger logger//
	) {
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
				logger.info(format("Exiting: {0} {1}", IBMAdapterRSA.class.getName(), "after"));
			}
			fillDefaultValuesAfter(ed, startTime, exception, logger);
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
}
