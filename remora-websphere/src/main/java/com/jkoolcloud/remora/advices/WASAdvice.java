/*
 * Copyright 2019-2020 NASTEL TECHNOLOGIES, INC.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

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
	 *            servlet response
	 * @param resp
	 *            servlet request
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
			@Advice.Local("ed") EntryDefinition ed, @Advice.Local("context") InterceptionContext ctx, //
			@Advice.Local("startTime") long startTime) //
	// @Advice.Local("remoraLogger") Logger logger)
	{
		try {
			ctx = prepareIntercept(WASAdvice.class, thiz, method, new Object[] { req, resp });
			if (!ctx.intercept) {
				return;
			}
			TaggedLogger logger = ctx.interceptorInstance.getLogger();

			ed = getEntryDefinition(ed, WASAdvice.class, ctx);
			startTime = fillDefaultValuesBefore(ed, stackThreadLocal, thiz, method, ctx);

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
						stackThreadLocal.get().setServer(cellName + "/" + nodeName + "/" + domainName);
					} catch (Exception e) {
						logger.error(e);
						stackThreadLocal.get().setServer(req.getLocalName());
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

			if (thiz instanceof WebApp) {
				try {
					ed.addPropertyIfExist("CONTEXT_PATH", ((WebApp) thiz).getContextPath());
				} catch (Throwable t) {
					logger.info("this" + thiz);
				}
			} else {
				logger.info("## This null");
			}

		} catch (Throwable t) {
			handleAdviceException(t, ctx);
		}
	}

	@Advice.OnMethodExit(onThrowable = Throwable.class)
	public static void after(@Advice.This Object obj, //
			@Advice.Origin Method method, //
			@Advice.Argument(0) ServletRequest req, //
			@Advice.Argument(1) ServletResponse resp, //
			@Advice.Thrown Throwable exception, //
			@Advice.Local("ed") EntryDefinition ed, @Advice.Local("context") InterceptionContext ctx, //
			@Advice.Local("startTime") long startTime) //
	// @Advice.Local("remoraLogger") Logger logger)
	{
		ctx = prepareIntercept(WASAdvice.class, obj, method, new Object[] { req, resp });
		if (!ctx.intercept) {
			return;
		}
		TaggedLogger logger = ctx.interceptorInstance.getLogger();
		boolean doFinally = true;
		try {
			if (ed == null) { // ed expected to be null if not created by entry, that's for duplicates
				logger.info(
						"EntryDefinition not exist, ctx.interceptorInstance, entry might be filtered out as duplicate or ran on test");
				doFinally = false;
				return;
			}

			fillDefaultValuesAfter(ed, startTime, exception, ctx);
			ed.addProperty("RespContext", resp.getContentType());
		} catch (Throwable t) {
			handleAdviceException(t, ctx);
		} finally {
			if (doFinally) {
				doFinally(ctx, obj.getClass());
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
