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

import static net.bytebuddy.matcher.ElementMatchers.nameStartsWith;
import static net.bytebuddy.matcher.ElementMatchers.none;

import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

import org.tinylog.TaggedLogger;

import com.jkoolcloud.remora.AdviceRegistry;
import com.jkoolcloud.remora.RemoraConfig;
import com.jkoolcloud.remora.core.CallStack;
import com.jkoolcloud.remora.core.EmptyStack;
import com.jkoolcloud.remora.core.EntryDefinition;
import com.jkoolcloud.remora.core.output.OutputManager;
import com.jkoolcloud.remora.filters.AdviceFilter;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.asm.TypeConstantAdjustment;
import net.bytebuddy.description.NamedElement;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.scaffold.MethodGraph;
import net.bytebuddy.dynamic.scaffold.TypeValidation;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.utility.JavaModule;

public abstract class BaseTransformers implements RemoraAdvice {

	@RemoraConfig.Configurable(configurableOnce = true)
	public static List<String> ignores;
	@RemoraConfig.Configurable
	public static int callStackLimit = 100;
	@RemoraConfig.Configurable(configurableOnce = true)
	public boolean load = true;
	@RemoraConfig.Configurable(configurableOnce = true)
	public boolean java15safe = false;

	@RemoraConfig.Configurable
	public boolean sendStackTrace;
	@RemoraConfig.Configurable
	private static int maxStackTraceElements = 30;

	@RemoraConfig.Configurable
	public boolean enabled = true;
	@RemoraConfig.Configurable
	public List<AdviceFilter> filters = new ArrayList<>(10);

	@RemoraConfig.Configurable
	public List<String> excludeProperties = new ArrayList<>(10);

	public static ThreadLocal<CallStack<EntryDefinition>> stackThreadLocal = new ThreadLocal<>();
	private final static AgentBuilder agentBuilder = new AgentBuilder.Default(
			new ByteBuddy().with(TypeValidation.DISABLED).with(MethodGraph.Compiler.ForDeclaredMethods.INSTANCE));
	@RemoraConfig.Configurable
	public static boolean checkCallRepeats = true;

	public List<AdviceListener> listeners = new ArrayList<>(5);

	public static class EnhancedElementMatcher<T extends TypeDescription>
			extends ElementMatcher.Junction.AbstractBase<T> {

		private final String[] interceptingClass;

		public EnhancedElementMatcher(String[] interceptingClass) {
			this.interceptingClass = interceptingClass;
		}

		@Override
		public boolean matches(T target) {
			for (String clazz : interceptingClass) {
				if (clazz.equals(target.getActualName())) {
					return true;
					// } else if (checkClass(target, clazz)) {
					// return true;
				} else {
					continue;
				}
			}
			return false;
		}
	}

	public AgentBuilder.Identified.Extendable getTransform() {
		AgentBuilder.Identified.Narrowable type = agentBuilder//
				// .with(listener) //
				.disableClassFormatChanges()//
				// .enableUnsafeBootstrapInjection() //
				.ignore(getClassIgnores()) //
				.type(getTypeMatcher());
		if (java15safe) {
			type.transform(new AgentBuilder.Transformer() {
				@Override
				public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder, TypeDescription typeDescription,
						ClassLoader classLoader, JavaModule module) {
					return builder.visit(TypeConstantAdjustment.INSTANCE);
				}
			});//
		}

		return type.transform(getAdvice());
	}

	public abstract ElementMatcher<TypeDescription> getTypeMatcher();

	public abstract AgentBuilder.Transformer getAdvice();

	public static void fillDefaultValuesAfter(EntryDefinition entryDefinition, long startTime,
			@Advice.Thrown Throwable exception, TaggedLogger logger) {
		if (entryDefinition.isChained()) {
			entryDefinition.setChained(false);
			return;
		}
		double duration = ((double) System.nanoTime() - startTime) / (double) TimeUnit.MICROSECONDS.toNanos(1L);
		invokeOnMethodFinished(entryDefinition.getAdviceClassClass(), duration);

		entryDefinition.setDuration((long) duration);

		entryDefinition.stop();

		if (exception != null) {
			handleInstrumentedMethodException(entryDefinition, exception, logger);
		}

		Stack<EntryDefinition> entryDefinitionStack = stackThreadLocal.get();
		if (entryDefinitionStack != null && entryDefinitionStack.size() >= 2) {
			EntryDefinition lastEntryDefinition = entryDefinitionStack.get(entryDefinitionStack.size() - 2);
			if (lastEntryDefinition != null) {
				entryDefinition.addProperty("PARENT", lastEntryDefinition.getId());
			}
		}
		if (!entryDefinition.isTransparent() && !(stackThreadLocal.get() instanceof EmptyStack)) {
			OutputManager.send(entryDefinition);
		}
	}

	public static void handleInstrumentedMethodException(EntryDefinition entryDefinition, Throwable exception,
			TaggedLogger logger) {
		entryDefinition.setException(exception);

		if (logger != null) {
			logger.info(
					format("Exception {} occurred in method {}", exception.getMessage(), entryDefinition.getClazz()));
		}
	}

	public static long fillDefaultValuesBefore(EntryDefinition entryDefinition,
			ThreadLocal<CallStack<EntryDefinition>> stackThreadLocal, Object thiz, Method method, TaggedLogger logger) {
		if (entryDefinition.isChained()) {
			return 0;
		}
		try {
			if (thiz != null) {
				entryDefinition.setClazz(thiz.getClass().getName());
			} else {
				if (logger != null) {
					logger.info("This not filled");
				}
			}

			if (method != null) {
				entryDefinition.setName(method.getName());
			} else {
				if (logger != null) {
					logger.info("#Method not filled");
				}
			}

			if (stackThreadLocal != null) {
				if (stackThreadLocal.get() == null) {
					CallStack<EntryDefinition> definitions = new CallStack<>(logger, callStackLimit);
					stackThreadLocal.set(definitions);
				}
				stackThreadLocal.get().push(entryDefinition);
			}

			entryDefinition.setThread(Thread.currentThread().toString());
			entryDefinition.setStartTime(System.currentTimeMillis());
			if (getAdviceInstance(entryDefinition.getAdviceClassClass()).sendStackTrace) {
				entryDefinition.setStackTrace(getStackTrace());
			}
			if (!entryDefinition.isTransparent() || !(stackThreadLocal.get() instanceof EmptyStack)) {
				OutputManager.send(entryDefinition);
			}
		} catch (Throwable t) {
			if (logger != null) {
				logger.error("####Advice error/fillDefaultValuesBefore: {}", t);
			}
		}
		return System.nanoTime();
	}

	private static void invokeOnIntercept(Class<?> adviceClass, Object thiz, Method method) {
		try {
			List<AdviceListener> listeners = AdviceRegistry.INSTANCE
					.getBaseTransformerByName(adviceClass.getSimpleName()).listeners;
			for (AdviceListener listener : listeners) {
				listener.onIntercept(adviceClass, thiz, method);
			}
		} catch (ClassNotFoundException e) {

		}

	}

	private static void invokeEventCreate(Class<?> adviceClass, EntryDefinition ed) {
		try {
			List<AdviceListener> listeners = AdviceRegistry.INSTANCE
					.getBaseTransformerByName(adviceClass.getSimpleName()).listeners;
			for (AdviceListener listener : listeners) {
				listener.onCreateEntity(adviceClass, ed);
			}
		} catch (ClassNotFoundException e) {

		}
	}

	private static void invokeOnError(Class<?> adviceClass, Throwable t) {
		try {
			List<AdviceListener> listeners = AdviceRegistry.INSTANCE
					.getBaseTransformerByName(adviceClass.getSimpleName()).listeners;
			for (AdviceListener listener : listeners) {
				listener.onAdviceError(adviceClass, t);
			}
		} catch (ClassNotFoundException e) {

		}
	}

	private static void invokeOnMethodFinished(Class<?> adviceClass, double elapseTime) {
		try {
			List<AdviceListener> listeners = AdviceRegistry.INSTANCE
					.getBaseTransformerByName(adviceClass.getSimpleName()).listeners;
			for (AdviceListener listener : listeners) {
				listener.onMethodFinished(adviceClass, elapseTime);
			}
		} catch (ClassNotFoundException e) {

		}
	}

	public static void registerListener(Class<? extends AdviceListener> adviceListener) {
		for (RemoraAdvice registeredAdvice : AdviceRegistry.INSTANCE.getRegisteredAdvices()) {
			if (registeredAdvice instanceof BaseTransformers) {
				try {
					((BaseTransformers) registeredAdvice).listeners.add(adviceListener.newInstance());
				} catch (InstantiationException e) {
				} catch (IllegalAccessException e) {
				}
			}
		}
	}

	public static String getStackTrace() {
		StackTraceElement[] stackTrace = new Exception().getStackTrace();
		int i = 0;
		StringBuilder sb = new StringBuilder();
		sb.append("Stack length: ");
		sb.append(stackTrace.length);
		sb.append("\n ");
		for (StackTraceElement element : stackTrace) {
			i++;
			if (i >= maxStackTraceElements) {
				break;
			}

			sb.append(element.getClassName());
			sb.append(".");
			sb.append(element.getMethodName());
			sb.append("()");
			sb.append("\r\n\t ");

		}
		return sb.toString();
	}

	public static void doFinally(TaggedLogger logger, Class<?> caller) {
		if (logger != null) {
			logger.debug("DoFinnaly");
		}
		try {

			if (stackThreadLocal != null) {
				Stack<EntryDefinition> entryDefinitions = stackThreadLocal.get();
				if (entryDefinitions != null) {
					EntryDefinition peek = entryDefinitions.peek();
					if (peek != null && caller != null) {

						if (Objects.equals(peek.getClazz(), caller.getName())) {
							entryDefinitions.pop();
						}

					}
					if (entryDefinitions.size() <= 0) {
						stackThreadLocal.remove();
						if (logger != null) {
							logger.info("Stack end;");
						}
					}
				}
			} else {
				if (logger != null) {
					logger.info("No stackThread");
				}
			}
		} catch (Exception e) {
			if (logger != null) {
				logger.info(e);
			}
		}
	}

	public static boolean isChainedClassInterception(Class<?> adviceClass, TaggedLogger logger,
			EntryDefinition lastED) {
		if (lastED == null) {
			return false;
		}
		try {
			if (adviceClass.getSimpleName().equals(lastED.getAdviceClass())) {
				if (logger != null) {
					logger.info(("Stack contains the same advice"));
				}
				return true;
			}
		} catch (Exception e) {
			if (logger != null) {
				logger.info("Can't check if advice stack has stacked common advices");
				logger.info(e);
			}
		}
		return false;
	}

	public static void handleAdviceException(Throwable t, String adviceName, TaggedLogger logger) {
		try {
			invokeOnError(AdviceRegistry.INSTANCE.getAdviceByName(adviceName).getClass(), t);
		} catch (ClassNotFoundException e) {
		}
		if (logger != null) {
			logger.info("{} threw an exception {} {}", adviceName, t.getMessage(), t.getClass().getName());
			logger.info(Arrays.toString(t.getStackTrace()));
		}
	}

	protected static ElementMatcher<NamedElement> getClassIgnores() {
		return nameStartsWith("net.openhft") //
				.or(nameStartsWith("java.lang")) //
				.or(nameStartsWith("com.jkoolcloud.remora")) //
				.or(nameStartsWith("net.bytebuddy")) //
				.or(getFromConfig());
	}

	private static ElementMatcher<NamedElement> getFromConfig() {
		ElementMatcher.Junction<NamedElement> ad = none();
		if (ignores == null) {
			return ad;
		}
		for (String ignore : ignores) {
			ad = ad.or(nameStartsWith(ignore));
			System.out.println("Will ignore classes " + ignore);
		}
		return ad;
	}

	@SuppressWarnings("unchecked")
	private static <T extends BaseTransformers> T getAdviceInstance(Class<T> tClass) {
		try {
			return (T) AdviceRegistry.INSTANCE.getAdviceByName(tClass.getSimpleName());
		} catch (ClassNotFoundException e) {
			return (T) AdviceRegistry.INSTANCE.getRegisteredAdvices().get(0);
		}
	}

	public static boolean intercept(Class<? extends BaseTransformers> tClass, Object thiz, Method method,
			TaggedLogger logger, Object... arguments) {
		invokeOnIntercept(tClass, thiz, method);
		if (stackThreadLocal.get() instanceof EmptyStack) {
			return false;
		}
		if (!getAdviceInstance(tClass).enabled) {
			return false;
		}
		for (AdviceFilter filter : getAdviceInstance(tClass).filters) {
			if (!filter.intercept(thiz, method, arguments)) {
				if (filter.excludeWholeStack()) {
					if (stackThreadLocal.get() == null || stackThreadLocal.get().isEmpty()) {
						stackThreadLocal.set(new EmptyStack(logger, callStackLimit));
					}
				}
				return false;
			}
		}
		return true;
	}

	protected abstract AgentBuilder.Listener getListener();

	public static String format(String pattern, Object... args) {
		return MessageFormat.format(pattern, args);
	}

	public static EntryDefinition getEntryDefinition(EntryDefinition ed, Class<? extends BaseTransformers> adviceClass,
			TaggedLogger logger) {
		if (ed != null) {
			return ed;
		}

		EntryDefinition lastED = null;
		CallStack<EntryDefinition> entryDefinitions = stackThreadLocal.get();
		if (entryDefinitions != null && !entryDefinitions.isEmpty()) {
			lastED = entryDefinitions.peek();
		}
		if (adviceClass.isAnnotationPresent(TransparentAdvice.class)) {
			if (lastED != null && lastED.isTransparent()) {
				if (logger != null) {
					logger.debug("Transparent advice, last ED is transparent, returning last {}", lastED.getId());
				}
				return lastED;
			} else {

				EntryDefinition entryDefinition = new EntryDefinition(adviceClass, checkCallRepeats);
				invokeEventCreate(adviceClass, entryDefinition);
				if (logger != null) {
					logger.debug("Transparent advice, no previous transparent advice, returning new {}",
							entryDefinition.getId());
				}
				entryDefinition.setTransparent();
				entryDefinition.setMode(EntryDefinition.Mode.STOP);
				return entryDefinition;
			}

		} else {
			if (lastED != null && lastED.isTransparent()) {
				if (logger != null) {
					logger.debug("Nontransparent advice, previous transparent advice, returning last {}",
							lastED.getId());
				}
				lastED.setAdviceClass(adviceClass);
				lastED.setTransparent(false);
				lastED.setMode(EntryDefinition.Mode.RUNNING);
				return lastED;
			} else {

				if (isChainedClassInterception(adviceClass, logger, lastED)) {
					lastED.setChained();
					if (logger != null) {
						logger.debug(
								"Nontransparent advice, previous non transparent advice, chained, returning last {}",
								lastED.getId());
					}
					return lastED;
				} else {

					EntryDefinition entryDefinition = new EntryDefinition(adviceClass, checkCallRepeats);
					invokeEventCreate(adviceClass, entryDefinition);
					if (logger != null) {
						logger.debug("Nontransparent advice, previous non transparent advice, returning new {}",
								entryDefinition.getId());
					}
					return entryDefinition;
				}
			}
		}
	}

	public static class TransformationLoggingListener extends AgentBuilder.Listener.Adapter {
		TaggedLogger logger;
		public final static String PREFIX = "[ByteBuddy]";

		public TransformationLoggingListener(TaggedLogger logger) {
			this.logger = logger;
		}

		@Override
		public void onTransformation(TypeDescription typeDescription, ClassLoader classLoader, JavaModule module,
				boolean loaded, DynamicType dynamicType) {
			if (logger == null) {
				System.out.println(format(PREFIX + " TRANSFORM {} [{}, {}, loaded={}]", typeDescription.getName(),
						classLoader, module, loaded));
			} else {
				logger.info(PREFIX + " TRANSFORM {} [{}, {}, loaded={}]", typeDescription.getName(), classLoader,
						module, typeDescription);

			}
		}

		@Override
		public void onError(String typeName, ClassLoader classLoader, JavaModule module, boolean loaded,
				Throwable throwable) {

			if (logger == null) {
				System.out.println(
						format(PREFIX + " ERROR {} [{}, {}, loaded={}] \n", typeName, classLoader, module, loaded));
				throwable.printStackTrace();
			} else {
				logger.info(PREFIX + " ERROR {} [{}, {}, loaded={}] \n", typeName, classLoader, module, loaded);
				logger.info(throwable.getMessage());
				logger.info(Arrays.toString(throwable.getStackTrace()));
			}

		}

	}

	public static class DiscoveryLoggingListener extends TransformationLoggingListener {

		public DiscoveryLoggingListener(TaggedLogger logger) {
			super(logger);
		}

		@Override
		public void onDiscovery(String typeName, ClassLoader classLoader, JavaModule module, boolean loaded) {
			if (logger == null) {
				System.out.println(
						format(PREFIX + " DISCOVERY {} [{}, {}, loaded={}]", typeName, classLoader, module, loaded));
			} else {
				logger.info(PREFIX + " DISCOVERY {} [{}, {}, loaded={}]", typeName, classLoader, module);

			}
		}

		@Override
		public void onIgnored(TypeDescription typeDescription, ClassLoader classLoader, JavaModule module,
				boolean loaded) {
			if (logger == null) {
				System.out.println(format(PREFIX + " IGNORED {} [{}, {}, loaded={}]", typeDescription, classLoader,
						module, loaded));
			} else {
				logger.info(PREFIX + " IGNORED {} [{}, {}, loaded={}]", typeDescription, classLoader, module);

			}
		}
	}

}
