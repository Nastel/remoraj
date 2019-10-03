package com.jkoolcloud.remora.advices;

import static net.bytebuddy.matcher.ElementMatchers.nameStartsWith;

import java.lang.reflect.Method;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import com.jkoolcloud.remora.core.EntryDefinition;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.NamedElement;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

public class JavaxHttpServlet extends BaseTransformers implements RemoraAdvice {
	private static final String ADVICE_NAME = "JavaxHttpServlet";
	public static String[] INTERCEPTING_CLASS = { "javax.servlet.http.HttpServlet" };
	public static String INTERCEPTING_METHOD = "service";

	private static ElementMatcher.Junction<NamedElement> methodMatcher() {
		return nameStartsWith(INTERCEPTING_METHOD);
	}

	static AgentBuilder.Transformer.ForAdvice advice = new AgentBuilder.Transformer.ForAdvice()
			.include(JavaxHttpServlet.class.getClassLoader())

			.advice(methodMatcher(), JavaxHttpServlet.class.getName());

	@Override
	public EnhancedElementMatcher<TypeDescription> getTypeMatcher() {
		return new EnhancedElementMatcher<>(INTERCEPTING_CLASS);
	}

	@Override
	public AgentBuilder.Transformer getAdvice() {
		return advice;
	}

	@Advice.OnMethodEnter
	public static void before(@Advice.This Object thiz, //
			@Advice.Argument(0) ServletRequest req, //
			@Advice.Argument(1) ServletResponse resp, //
			@Advice.Origin Method method, //
			@Advice.Local("ed") EntryDefinition ed, //
			@Advice.Local("starttime") long starttime) //
	{
		try {
			System.out.println("X");
			if (isChainedClassInterception(JavaxHttpServlet.class)) {
				return; // return if its chain of same
			}
			if (ed == null) {
				ed = new EntryDefinition(JavaxHttpServlet.class);
			}
			ed.addProperty("Working", "true");
			starttime = fillDefaultValuesBefore(ed, stackThreadLocal, thiz, method);

			if (req != null) {
				try {
					// if (req.getServletContext() != null) {
					// ed.addProperty("Resource", req.getServletContext().getContextPath()); CANT USE
					// }
					ed.addPropertyIfExist("CLIENT", req.getRemoteAddr());
					ed.addPropertyIfExist("SERVER", req.getLocalName());
				} catch (Throwable t) {
					System.out.println("req" + req);
					t.printStackTrace();
				}

			} else {
				System.out.println("## Request null");
			}
			// if (thiz != null) {
			// try {
			// ed.addPropertyIfExist("CONTEXT_PATH", thiz.getContextPath());
			// } catch (Throwable t) {
			// System.out.println("this" + thiz);
			// t.printStackTrace();
			// }
			// } else {
			// System.out.println("## This null");
			// }

		} catch (Throwable t) {
			handleAdviceException(t, ADVICE_NAME);
		}
	}

	// private static boolean safe(Runnable function) {
	// try {
	// function.run();
	// return true;
	// } catch (Throwable e) {
	// StackTraceElement stackTraceElement = e.getStackTrace()[2];
	// System.out.println("Advice safe failure");
	// System.out.print("#### " + e.getMessage() + " ");
	// System.out.println(stackTraceElement.getFileName() + ":" + stackTraceElement.getLineNumber() + " & "
	// + stackTraceElement.getClassName() + "." + stackTraceElement.getMethodName());
	// return false;
	// }
	// }

	@Advice.OnMethodExit(onThrowable = Throwable.class)
	public static void after(@Advice.This Object obj, //
			@Advice.Origin Method method, //
			@Advice.Argument(0) ServletRequest req, //
			@Advice.Argument(1) ServletResponse resp, //
			@Advice.Thrown Throwable exception, //
			@Advice.Local("ed") EntryDefinition ed, //
			@Advice.Local("starttime") long starttime) {
		try {
			System.out.println("XE");
			fillDefaultValuesAfter(ed, starttime, exception);
			ed.addProperty("RespContext", resp.getContentType());
		} catch (Throwable t) {
			handleAdviceException(t, ADVICE_NAME);
		} finally {
			doFinally();
		}

	}

}
