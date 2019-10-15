#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.advices;

import net.bytebuddy.asm.Advice;
import static net.bytebuddy.matcher.ElementMatchers.*;

import java.lang.reflect.Method;
import java.util.logging.Logger;

import com.jkoolcloud.remora.RemoraConfig;
import com.jkoolcloud.remora.core.EntryDefinition;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.NamedElement;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

public class ${adviceClassName}Advice extends BaseTranformers implements RemoraAdvice {


	private static final String ADVICE_NAME = "${adviceClassName}Advice";
	public static String[] INTERCEPTING_CLASS = {"<CHANGE HERE>"};
	public static String INTERCEPTING_METHOD = "<CHANGE HERE>";

	@RemoraConfig.Configurable
	public static boolean logging = true;
		public static Logger logger;
	static {
		logger = Logger.getLogger(${adviceClassName}.class.getName());
		configureAdviceLogger(logger);
	}

	/**
	 *  Method matcher intended to match intercepted class method/s to
	 *  instrument. See (@ElementMatcher) for available method matches.
	 */

	private static ElementMatcher.Junction<NamedElement> methodMatcher() {
		return named(INTERCEPTING_METHOD);
	}

    /**
     * Type matcher should find the class intended for instrumentation
     * See (@ElementMatcher) for available matches.
     */

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
	public static void before(@Advice.This Object thiz, //
			@Advice.AllArguments Object[] arguments, //
			@Advice.Origin Method method, //
			@Advice.Local("ed") EntryDefinition ed, //
			@Advice.Local("startTime") long startTime) {
		try {
            if (ed == null) {
                ed = new EntryDefinition(JMSSendAdvice.class);
            }
            if (logging) {
               logger.info(format("Entering: {0} {1}",JMSCreateConnectionAdvice.class.getName(), "before");
            }
			startTime = fillDefaultValuesBefore(ed, stackThreadLocal, thiz, method, logger);
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
	 * @param ed    {@link EntryDefinition} passed along the method (from before method)
	 * @param startTime startTime passed along the method
	 */

	@Advice.OnMethodExit(onThrowable = Throwable.class)
	public static void after(@Advice.This Object obj, //
			@Advice.Origin Method method, //
            @Advice.AllArguments Object[] arguments, //
			// @Advice.Return Object returnValue, // //TODO needs separate Advice capture for void type
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
                logger.info(format("Exiting: {0} {1}",${adviceClassName}Advice.class.getName(), "after");
            }
                fillDefaultValuesAfter(ed, startTime, exception);
        } catch (Throwable t) {
            handleAdviceException(t, ADVICE_NAME, logger);
		} finally {
			if (doFinally) {
				doFinally();
			}
		}

	}

}