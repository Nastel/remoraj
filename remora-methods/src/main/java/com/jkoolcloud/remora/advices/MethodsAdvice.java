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

import static com.jkoolcloud.remora.Remora.REMORA_PATH;
import static java.nio.file.StandardOpenOption.*;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.jkoolcloud.remora.RemoraConfig;
import com.jkoolcloud.remora.core.EntryDefinition;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.bytebuddy.matcher.ElementMatcher;

public class MethodsAdvice extends BaseTransformers implements RemoraAdvice {

	public static final String ADVICE_NAME = "MethodsAdvice";
	public static final String CONFIG_BUSSINESS_METHOD_PROPERTIES = "/config/remora-methods.properties";
	public static final String REMORA_BASE_PATH = System.getProperty(REMORA_PATH);
	public static final Path CONFIGURATION_PATH = Paths.get(REMORA_BASE_PATH + CONFIG_BUSSINESS_METHOD_PROPERTIES);

	@RemoraConfig.Configurable
	public int maxArgumentLength = 128;

	public Set<String> classAndMethodList = new HashSet<>();
	public Set<String> instrumentedClasses = new HashSet<>();

	/**
	 * Method matcher intended to match intercepted class method/s to instrument. See (@ElementMatcher) for available
	 * method matches.
	 */

	private ElementMatcher<? super MethodDescription> methodMatcher() {
		return new ElementMatcher<MethodDescription>() {

			@Override
			public boolean matches(MethodDescription target) {
				return classAndMethodList.contains(target.getDeclaringType().getTypeName() + "." + target.getName());
			}
		};
	}

	/**
	 * Type matcher should find the class intended for instrumentation See (@ElementMatcher) for available matches.
	 */

	@Override
	public ElementMatcher<TypeDescription> getTypeMatcher() {
		return new ElementMatcher<TypeDescription>() {
			@Override
			public boolean matches(TypeDescription target) {
				return instrumentedClasses.contains(target.getName());
			}
		};
	}

	@Override
	public AgentBuilder.Transformer getAdvice() {
		return advice;
	}

	AgentBuilder.Transformer.ForAdvice advice = new AgentBuilder.Transformer.ForAdvice()
			.include(MethodsAdvice.class.getClassLoader()).include(RemoraConfig.INSTANCE.classLoader)//
			.advice(methodMatcher(), MethodsAdvice.class.getName());

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
	public static void before(@Advice.This(optional = true) Object thiz, //
			@Advice.AllArguments(typing = Assigner.Typing.DYNAMIC) Object[] arguments, //
			@Advice.Origin Method method, //
			@Advice.Local("ed") EntryDefinition ed, //
			@Advice.Local("context") InterceptionContext ctx, //
			@Advice.Local("startTime") long startTime) {
		try {
			ctx = prepareIntercept(MethodsAdvice.class, thiz, method, arguments);
			if (!ctx.intercept) {
				return;
			}
			ed = getEntryDefinition(ed, MethodsAdvice.class, ctx);

			int i = 0;
			for (Parameter param : method.getParameters()) {
				String value = String.valueOf(arguments[i]);
				int maxArgumentLength = ((MethodsAdvice) ctx.interceptorInstance).maxArgumentLength;
				if (value.length() > maxArgumentLength) {
					ed.addPropertyIfExist(param.getName(), value.substring(0, maxArgumentLength));
				} else {
					ed.addPropertyIfExist(param.getName(), value);
				}
				i++;
			}

			startTime = fillDefaultValuesBefore(ed, stackThreadLocal, thiz, method, ctx);
		} catch (Throwable t) {
			handleAdviceException(t, ctx);
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
	 * @param ed
	 *            {@link EntryDefinition} passed along the method (from before method)
	 * @param startTime
	 *            startTime passed along the method
	 */

	@Advice.OnMethodExit(onThrowable = Throwable.class)
	public static void after(@Advice.This(optional = true) Object obj, //
			@Advice.Origin Method method, //
			@Advice.AllArguments(typing = Assigner.Typing.DYNAMIC) Object[] arguments, //
			@Advice.Return(typing = Assigner.Typing.DYNAMIC) Object returnValue, @Advice.Thrown Throwable exception, //
			@Advice.Local("ed") EntryDefinition ed, //
			@Advice.Local("context") InterceptionContext ctx, //
			@Advice.Local("startTime") long startTime) {
		boolean doFinally = true;
		try {
			ctx = prepareIntercept(MethodsAdvice.class, obj, method, arguments);
			if (!ctx.intercept) {
				return;
			}
			doFinally = checkEntryDefinition(ed, ctx);

			int maxArgumentLength = ((MethodsAdvice) ctx.interceptorInstance).maxArgumentLength;
			if (returnValue != null) {
				String value = String.valueOf(returnValue);
				if (value.length() > maxArgumentLength) {
					ed.addPropertyIfExist(method.getName(), value.substring(0, maxArgumentLength));
				} else {
					ed.addPropertyIfExist(method.getName(), value);
				}
			}

			ed.addPropertyIfExist(method.getName(), String.valueOf(returnValue));

			fillDefaultValuesAfter(ed, startTime, exception, ctx);
		} catch (Throwable t) {
			handleAdviceException(t, ctx);
		} finally {
			if (doFinally) {
				doFinally(ctx, obj == null ? method.getDeclaringClass() : obj.getClass());
			}
		}

	}

	@Override
	public void install(Instrumentation instrumentation) {
		readConfigurationFile();
		super.install(instrumentation);

		logger.info("Will instrument {}, as specified in {}", instrumentedClasses, CONFIG_BUSSINESS_METHOD_PROPERTIES);
		logger.debug("Will instrument method {}", classAndMethodList);
	}

	private void readConfigurationFile() {
		try (Stream<String> lines = Files.lines(CONFIGURATION_PATH)) {
			fillClassAndMethodList(lines);
		} catch (IOException e) {
			logger.error(e);
		}

	}

	protected void fillClassAndMethodList(Stream<String> lines) {
		classAndMethodList = lines.map(line -> line.substring(0, line.indexOf('('))).collect(Collectors.toSet());
		instrumentedClasses = classAndMethodList.stream().map(entry -> entry.substring(0, entry.lastIndexOf('.')))
				.collect(Collectors.toSet());
	}

	public void writeConfigurationFiles() throws IOException {
		Files.write(CONFIGURATION_PATH,
				classAndMethodList.stream().map(line -> line + "()").collect(Collectors.toList()),
				new java.nio.file.StandardOpenOption[] { WRITE, CREATE, TRUNCATE_EXISTING });
	}

	@Override
	public String getName() {
		return ADVICE_NAME;
	}

}
