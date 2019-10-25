package com.jkoolcloud.remora.matchers;

import net.bytebuddy.matcher.ElementMatcher;

public class ClassLoaderMatcher extends ElementMatcher.Junction.AbstractBase<ClassLoader> {

	private final String name;

	private ClassLoaderMatcher(String name) {
		this.name = name;
	}

	public static ElementMatcher.Junction.AbstractBase<ClassLoader> classLoaderWithName(String name) {
		return new ClassLoaderMatcher(name);

	}

	public static ElementMatcher.Junction.AbstractBase<ClassLoader> isReflectionClassLoader() {
		return new ClassLoaderMatcher("sun.reflect.DelegatingClassLoader");
	}

	@Override
	public boolean matches(ClassLoader target) {
		return target != null && name.equals(target.getClass().getName());
	}
}