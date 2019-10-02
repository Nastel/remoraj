package com.jkoolcloud.remora.advices;


import com.jkoolcloud.remora.core.EntryDefinition;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.NamedElement;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;

import static net.bytebuddy.matcher.ElementMatchers.nameStartsWith;

public class SimpleTest extends BaseTranformers implements RemoraAdvice{

    private static final String ADVICE_NAME = "SimpleTest";
    public static String[] INTERCEPTING_CLASS = {"lt.slabs.javaam.JustATest"};
    public static String INTERCEPTING_METHOD = "instrumentedMethod";

    static AgentBuilder.Transformer.ForAdvice advice = new AgentBuilder.Transformer.ForAdvice()
            .include(GeneralAdvice.class.getClassLoader())

            .advice(methodMatcher(), SimpleTest.class.getName());

    private static ElementMatcher.Junction<NamedElement> methodMatcher() {
        return nameStartsWith(INTERCEPTING_METHOD);
    }

    public SimpleTest() {
        ClassLoader classLoader = getClass().getClassLoader();
        System.out.println(classLoader + " and parent: " + (classLoader == null ? "null" : classLoader.getParent()));

    }

    @Override
    public BaseTranformers.EnhancedElementMatcher<TypeDescription> getTypeMatcher() {
        return new BaseTranformers.EnhancedElementMatcher<>(INTERCEPTING_CLASS);
    }

    @Override
    public AgentBuilder.Transformer getAdvice() {
        return advice;
    }

    @Advice.OnMethodEnter
    public static void before(@Advice.This Object thiz, //
                              @Advice.Argument(0) Object uri, //
                              @Advice.Argument(1) Object arguments, //
                              @Advice.Origin Method method, //
                              @Advice.Local("ed") EntryDefinition ed, //
                              @Advice.Local("starttime") long starttime //
    ) {
        try {
            if (ed == null) {
                ed = new EntryDefinition(SimpleTest.class);
            }

            starttime = fillDefaultValuesBefore(ed, stackThreadLocal, thiz, method);
            Class.forName("Blah");
            ed.addProperty("URI", uri.toString());
            ed.addProperty("Arg", arguments.toString());

        } catch (Throwable t) {
            handleAdviceException(t, ADVICE_NAME);
        }
    }

    @Advice.OnMethodExit(onThrowable = Throwable.class)
    public static void after(@Advice.This Object obj, //
                             @Advice.Origin Method method, //
                             // @Advice.Return Object returnValue, // //TODO needs separate Advice capture for void type
                             @Advice.Thrown Throwable exception, //
                             @Advice.Local("ed") EntryDefinition ed, //
                             @Advice.Local("starttime") long starttime) {
        try {
            fillDefaultValuesAfter(ed, starttime, exception);
        } finally {
            doFinally();
        }

    }

}
