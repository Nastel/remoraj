package com.jkoolcloud.remora.advices;

import static net.bytebuddy.matcher.ElementMatchers.nameStartsWith;
import static net.bytebuddy.matcher.ElementMatchers.named;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.DispatcherType;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.tinylog.Logger;
import org.tinylog.TaggedLogger;

import com.jkoolcloud.remora.RemoraConfig;
import com.jkoolcloud.remora.core.CallStack;
import com.jkoolcloud.remora.core.EntryDefinition;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.NamedElement;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

public class JavaxHttpServlet extends BaseTransformers implements RemoraAdvice {
	public static final String ADVICE_NAME = "JavaxHttpServlet";
	public static String[] INTERCEPTING_CLASS = { "javax.servlet.http.HttpServlet" };
	public static String INTERCEPTING_METHOD = "service";

	@RemoraConfig.Configurable
	public static boolean logging = true;
	public static TaggedLogger logger;
	static AgentBuilder.Transformer.ForAdvice advice = new AgentBuilder.Transformer.ForAdvice()
			.include(JavaxHttpServlet.class.getClassLoader()) //
			.include(RemoraConfig.INSTANCE.classLoader) //
			.advice(methodMatcher(), JavaxHttpServlet.class.getName());
	@RemoraConfig.Configurable
	public static String cookiePrefix = "CKIE_";
	@RemoraConfig.Configurable
	public static String headerPrefix = "HDR_";
	@RemoraConfig.Configurable
	public static boolean attachCorrelator = true;
	@RemoraConfig.Configurable
	public static String headerCorrIDName = "REMORA_CORR";

	/**
	 * Method matcher intended to match intercepted class method/s to instrument. See (@ElementMatcher) for available
	 * method matches.
	 */
	private static ElementMatcher.Junction<NamedElement> methodMatcher() {
		return nameStartsWith(INTERCEPTING_METHOD);
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
	// @Advice.Local("remoraLogger") Logger logger) //
	{
		try {
			if (logging) {
				logger.info("Entering: {0} {1} from {2}", JavaxHttpServlet.class.getSimpleName(), "before",
						thiz.getClass().getName());
			}
			if (isChainedClassInterception(JavaxHttpServlet.class, logger)) {
				return; // return if its chain of same
			}
			if (ed == null) {
				ed = new EntryDefinition(JavaxHttpServlet.class);
			}
			ed.addProperty("Working", "true");
			startTime = fillDefaultValuesBefore(ed, stackThreadLocal, thiz, method, logger);

			if (req != null && req instanceof HttpServletRequest && req.getDispatcherType() == DispatcherType.REQUEST) {
				try {
					ed.addPropertyIfExist("CLIENT", req.getRemoteAddr());
					ed.addPropertyIfExist("SERVER", req.getLocalName());

					HttpServletRequest request = (HttpServletRequest) req;

					ed.addPropertyIfExist("PROTOCOL", request.getProtocol());
					ed.addPropertyIfExist("METHOD", request.getMethod());
					ed.addPropertyIfExist("SECURE", request.isSecure());
					ed.addPropertyIfExist("SCHEME", request.getScheme());
					ed.addPropertyIfExist("SERVER", request.getServerName());
					ed.addPropertyIfExist("PORT", request.getServerPort());
					String requestURI = request.getRequestURI();
					ed.addPropertyIfExist("RESOURCE", requestURI);

					ed.setResource(requestURI, EntryDefinition.ResourceType.HTTP);

					if (stackThreadLocal != null && stackThreadLocal.get() != null
							&& stackThreadLocal.get() instanceof CallStack) {
						Pattern compile = Pattern.compile("\\/(.\\w*)\\/");
						Matcher matcher = compile.matcher(requestURI);
						if (matcher.find()) {
							((CallStack) stackThreadLocal.get()).setApplication(matcher.group(0));
						}
					}

					ed.addPropertyIfExist("QUERY", request.getQueryString());
					ed.addPropertyIfExist("CONTENT_TYPE", request.getHeader("Content-Type"));

					if (request.getCookies() != null) {
						for (Cookie cookie : request.getCookies()) {
							ed.addPropertyIfExist(cookiePrefix + cookie.getName(), cookie.getValue());
						}
					}
					Enumeration headerNames = request.getHeaderNames();
					if (headerNames != null) {
						while (headerNames.hasMoreElements()) {
							String headerName = (String) headerNames.nextElement();
							Enumeration<String> headerValues = request.getHeaders(headerName);
							StringBuilder headerValue = new StringBuilder();
							while (headerValues.hasMoreElements()) {
								headerValue.append(headerValues.nextElement());
								if (headerValues.hasMoreElements()) {
									headerValue.append(";");
								}
							}
							ed.addPropertyIfExist(headerPrefix + headerName, headerValue.toString());
						}
					}
					if (attachCorrelator && resp != null && resp instanceof HttpServletResponse) {
						if (((HttpServletRequest) req).getHeader(headerCorrIDName) == null) {
							((HttpServletResponse) resp).addHeader(headerCorrIDName, ed.getId());
							logger.info("Added header: " + headerCorrIDName + ed.getId());
						} else {
							((HttpServletResponse) resp).addHeader(headerCorrIDName,
									((HttpServletRequest) req).getHeader(headerCorrIDName));
						}
					}
				} catch (Throwable t) {
					logger.info("Failed getting some of properties" + req);
				}

			} else {
				logger.info("## Request null");
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
	 * @param req
	 * @param resp
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
				logger.info(format("Exiting: {0} {1}", JavaxHttpServlet.class.getName(), "after"));
			}
			fillDefaultValuesAfter(ed, startTime, exception, logger);

			ed.addProperty("RespContext", resp.getContentType());
		} catch (Throwable t) {
			handleAdviceException(t, ADVICE_NAME, logger);
		} finally {
			if (doFinally) {
				doFinally();
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
	public String getName() {
		return ADVICE_NAME;
	}

	@Override
	public void install(Instrumentation inst) {
		logger = Logger.tag(ADVICE_NAME);
		getTransform().with(getListener()).installOn(inst);
	}
}
