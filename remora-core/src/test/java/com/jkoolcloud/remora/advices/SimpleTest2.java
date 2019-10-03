package com.jkoolcloud.remora.advices;

import static net.bytebuddy.matcher.ElementMatchers.nameStartsWith;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.NamedElement;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

public class SimpleTest2 extends BaseTransformers implements RemoraAdvice {

	public static String[] INTERCEPTING_CLASS = { "java.net.HttpURLConnection" };
	public static String INTERCEPTING_METHOD = "getResponseCode";

	static AgentBuilder.Transformer.ForAdvice advice = new AgentBuilder.Transformer.ForAdvice()
			.include(GeneralAdvice.class.getClassLoader())
			.advice(methodMatcher(), "com.jkoolcloud.javaam.advices.GeneralAdvice");

	private static ElementMatcher.Junction<NamedElement> methodMatcher() {
		return nameStartsWith(INTERCEPTING_METHOD);
	}

	@Override
	public BaseTransformers.EnhancedElementMatcher<TypeDescription> getTypeMatcher() {
		return new BaseTransformers.EnhancedElementMatcher<>(INTERCEPTING_CLASS);
	}

	@Override
	public AgentBuilder.Transformer getAdvice() {
		return advice;
	}

}
