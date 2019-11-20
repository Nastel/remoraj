package com.jkoolcloud.remora.advices;

import static net.bytebuddy.matcher.ElementMatchers.named;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.tinylog.Logger;
import org.tinylog.TaggedLogger;

import com.ibm.websphere.management.AdminService;
import com.ibm.ws.webcontainer.srt.SRTServletRequest;
import com.ibm.ws.webcontainer.webapp.WebApp;
import com.jkoolcloud.remora.RemoraConfig;
import com.jkoolcloud.remora.core.CallStack;
import com.jkoolcloud.remora.core.EntryDefinition;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.NamedElement;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

public class WASAdvice extends BaseTransformers implements RemoraAdvice {
	public static final String ADVICE_NAME = "WASAdvice";
	public static String[] INTERCEPTING_CLASS = { "com.ibm.ws.webcontainer.webapp.WebApp",
			"com.ibm.ws.webcontainer.servlet.ServletWrapper" };
	public static String INTERCEPTING_METHOD = "handleRequest";

	@RemoraConfig.Configurable
	public static boolean load = true;
	@RemoraConfig.Configurable
	public static boolean logging = false;
	public static TaggedLogger logger;

	/**
	 * Method matcher intended to match intercepted class method/s to instrument. See (@ElementMatcher) for available
	 * method matches.
	 */

	private static ElementMatcher.Junction<NamedElement> methodMatcher() {
		return named(INTERCEPTING_METHOD);
	}

	static AgentBuilder.Transformer.ForAdvice advice = new AgentBuilder.Transformer.ForAdvice()
			.include(WASAdvice.class.getClassLoader())//
			.include(RemoraConfig.INSTANCE.classLoader) //
			.advice(methodMatcher(), WASAdvice.class.getName());

	/**
	 * Type matcher should find the class intended for instrumentation See (@ElementMatcher) for available matches.
	 */

	@Override
	public ElementMatcher<TypeDescription> getTypeMatcher() {
		return new BaseTransformers.EnhancedElementMatcher<>(INTERCEPTING_CLASS);
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
	 * @param req
	 * @param resp
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
			@Advice.Argument(0) ServletRequest req, //
			@Advice.Argument(1) ServletResponse resp, //
			@Advice.Origin Method method, //
			@Advice.Local("ed") EntryDefinition ed, //
			@Advice.Local("startTime") long startTime) //
	// @Advice.Local("remoraLogger") Logger logger)
	{
		try {
			if (logging) {
				logger.info("Entering: {0} {1} from {2}", WASAdvice.class.getSimpleName(), "before",
						thiz.getClass().getName());
			}
			if (isChainedClassInterception(WASAdvice.class, logging ? logger : null)) {
				return; // return if its chain of same
			}
			if (ed == null) {
				ed = new EntryDefinition(WASAdvice.class);
			}
			startTime = fillDefaultValuesBefore(ed, stackThreadLocal, thiz, method, logging ? logger : null);

			if (req != null) {
				try {
					// if (req.getServletContext() != null) {
					// ed.addProperty("Resource", req.getServletContext().getContextPath()); CANT USE
					// }
					ed.addPropertyIfExist("CLIENT", req.getRemoteAddr());
					ed.addPropertyIfExist("SERVER", req.getLocalName());

					try {
						Method getAdminService = Class.forName("com.ibm.websphere.management.AdminServiceFactory")
								.getMethod("getAdminService");
						AdminService adminService = (AdminService) getAdminService.invoke(null);
						String cellName = adminService.getCellName();
						String nodeName = adminService.getNodeName();
						String domainName = adminService.getDomainName();
						ed.addPropertyIfExist("CELL", cellName);
						ed.addPropertyIfExist("NODE", nodeName);
						ed.addPropertyIfExist("DOMAIN", domainName);
						((CallStack) stackThreadLocal.get()).setServer(cellName + "/" + nodeName + "/" + domainName);
					} catch (Exception e) {
						logger.error(e);
						((CallStack) stackThreadLocal.get()).setServer(req.getLocalName());
					}

				} catch (Throwable t) {
					logger.info("Some of req failed" + req);
				}
				if (req instanceof SRTServletRequest) {
					ed.addPropertyIfExist("RESOURCE", ((SRTServletRequest) req).getEncodedRequestURI());
				}

			} else {
				logger.info("## Request null");
			}

			if (thiz != null && thiz instanceof WebApp) {
				try {
					ed.addPropertyIfExist("CONTEXT_PATH", ((WebApp) thiz).getContextPath());
				} catch (Throwable t) {
					logger.info("this" + thiz);
				}
			} else {
				logger.info("## This null");
			}

		} catch (Throwable t) {
			handleAdviceException(t, ADVICE_NAME, logging ? logger : null);
		}
	}

	@Advice.OnMethodExit(onThrowable = Throwable.class)
	public static void after(@Advice.This Object obj, //
			@Advice.Origin Method method, //
			@Advice.Argument(0) ServletRequest req, //
			@Advice.Argument(1) ServletResponse resp, //
			@Advice.Thrown Throwable exception, //
			@Advice.Local("ed") EntryDefinition ed, //
			@Advice.Local("startTime") long startTime) //
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
				logger.info(format("Exiting: {0} {1}", WASAdvice.class.getName(), "after"));
			}
			fillDefaultValuesAfter(ed, startTime, exception, logging ? logger : null);
			ed.addProperty("RespContext", resp.getContentType());
		} catch (Throwable t) {
			handleAdviceException(t, ADVICE_NAME, logging ? logger : null);
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
