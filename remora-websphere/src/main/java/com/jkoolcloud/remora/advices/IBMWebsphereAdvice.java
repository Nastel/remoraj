package com.jkoolcloud.remora.advices;

import com.ibm.ws.webcontainer.srt.SRTServletRequest;
import com.ibm.ws.webcontainer.webapp.WebApp;
import com.jkoolcloud.remora.core.EntryDefinition;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.NamedElement;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.jetbrains.annotations.NotNull;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;

import static net.bytebuddy.matcher.ElementMatchers.nameStartsWith;

public class IBMWebsphereAdvice extends BaseTranformers implements RemoraAdvice{
    private static final String ADVICE_NAME = "IBMWebsphereAdvice";
    public static String[] INTERCEPTING_CLASS = {"com.ibm.ws.webcontainer.webapp.WebApp", "com.ibm.ws.webcontainer.servlet.ServletWrapper"};
    public static String INTERCEPTING_METHOD = "handleRequest";
    private EntryDefinition ed;

    @NotNull
    private static ElementMatcher.Junction<NamedElement> methodMatcher() {
        return nameStartsWith(INTERCEPTING_METHOD);
    }

    static AgentBuilder.Transformer.ForAdvice advice = new AgentBuilder.Transformer.ForAdvice()
            .include(IBMWebsphereAdvice.class.getClassLoader())
            .advice(methodMatcher(), IBMWebsphereAdvice.class.getName());

    @Override
    @NotNull
    public BaseTranformers.EnhancedElementMatcher<TypeDescription> getTypeMatcher() {
        return new BaseTranformers.EnhancedElementMatcher<>(INTERCEPTING_CLASS);
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
                              @Advice.Local("starttime") long starttime) {
        try {
            System.out.println("R");
            if (isChainedClassInterception(IBMWebsphereAdvice.class)) return; // return if its chain of same
            if (ed == null) {
                ed = new EntryDefinition(IBMWebsphereAdvice.class);
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
                if (req instanceof SRTServletRequest) {
                    ed.addPropertyIfExist("RESOURCE", ((SRTServletRequest) req).getEncodedRequestURI());
                }

            } else {
                System.out.println("## Request null");
            }

			if (thiz != null && thiz instanceof WebApp) {
				try {
					ed.addPropertyIfExist("CONTEXT_PATH", ((WebApp)thiz).getContextPath());
				} catch (Throwable t) {
					System.out.println("this" + thiz);
					t.printStackTrace();
				}
			} else {
				System.out.println("## This null");
			}

        } catch (Throwable t) {
            handleAdviceException(t, ADVICE_NAME);
        }
    }

    @Advice.OnMethodExit(onThrowable = Throwable.class)
    public static void after(@Advice.This Object obj, //
                             @Advice.Origin Method method, //
                             @Advice.Argument(0) ServletRequest req, //
                             @Advice.Argument(1) ServletResponse resp, //
                             @Advice.Thrown Throwable exception,//
                             @Advice.Local("ed") EntryDefinition ed, //
                             @Advice.Local("starttime") long starttime) {

        try {
            System.out.println("RE");
            fillDefaultValuesAfter(ed, starttime, exception);
            ed.addProperty("RespContext", resp.getContentType());
        } catch (Throwable t) {
            handleAdviceException(t, ADVICE_NAME);
        } finally {
            doFinally();
        }

    }


}
