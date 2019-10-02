package com.jkoolcloud.remora.advices;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.NamedElement;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.jetbrains.annotations.NotNull;

import java.lang.instrument.Instrumentation;

import static net.bytebuddy.matcher.ElementMatchers.nameStartsWith;

public class SimpleTest2 extends BaseTranformers implements RemoraAdvice {

	public static String[] INTERCEPTING_CLASS = { "java.net.HttpURLConnection" };
	public static String INTERCEPTING_METHOD = "getResponseCode";

	static AgentBuilder.Transformer.ForAdvice advice = new AgentBuilder.Transformer.ForAdvice()
			.include(GeneralAdvice.class.getClassLoader())
			.advice(methodMatcher(), "com.jkoolcloud.javaam.advices.GeneralAdvice");

	@NotNull
	private static ElementMatcher.Junction<NamedElement> methodMatcher() {
		return nameStartsWith(INTERCEPTING_METHOD);
	}

	@Override
	@NotNull
	public BaseTranformers.EnhancedElementMatcher<TypeDescription> getTypeMatcher() {
		return new BaseTranformers.EnhancedElementMatcher<>(INTERCEPTING_CLASS);
	}

	@Override
	public AgentBuilder.Transformer getAdvice() {
		return advice;
	}

}
