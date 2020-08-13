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

import static net.bytebuddy.matcher.ElementMatchers.*;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

import org.tinylog.Level;
import org.tinylog.Logger;
import org.tinylog.TaggedLogger;

import com.jkoolcloud.remora.AdviceRegistry;
import com.jkoolcloud.remora.RemoraConfig;
import com.jkoolcloud.remora.adviceListeners.AdviceListener;
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
import net.bytebuddy.matcher.LatentMatcher;
import net.bytebuddy.utility.JavaModule;

/**
 * Base class for {@link RemoraAdvice} witch is inherited by almost all of the advices. Here is wehre all the common
 * manipulation for intercepted methods occurs:
 *
 * <p>
 * <ul>
 * <li>Advice listeners events
 * <li>Filtering
 * <li>Enabled/dissabled featur
 * <li>Creating {@link EntryDefinition}
 * <li>Sending {@link com.jkoolcloud.remora.core.Entry} and {@link com.jkoolcloud.remora.core.Exit}
 * <li>Creating {@link CallStack} and correalting events
 * </ul>
 * <p>
 * the entity {@link EntryDefinition} is created
 */
public abstract class BaseTransformers implements RemoraAdvice, Loggable {
	/**
	 * List of qualified class name begginings to be ignored.
	 */
	@RemoraConfig.Configurable(configurableOnce = true)
	public static List<String> ignores;

	/**
	 * Call stack max value.
	 */
	@RemoraConfig.Configurable
	public static int callStackLimit = 100;

	/**
	 * Property that loads particualar advice. If set to false it will be ignored for loading, to load such advice you
	 * need to restart the application
	 */
	@RemoraConfig.Configurable(configurableOnce = true)
	public boolean load = true;

	/**
	 * Enabled instrumented classes to be compiled with version prior to Java 1.5. This needed for instrumenting
	 * Websphere 8.5.5 with Remora-stream
	 */
	@RemoraConfig.Configurable(configurableOnce = true)
	public boolean java15safe = false;

	/**
	 * If enabled Collects StackTrace of Advice intercepted methods.
	 */
	@RemoraConfig.Configurable
	public boolean sendStackTrace;

	/**
	 * Max element to collect for StackTrace.
	 */
	@RemoraConfig.Configurable
	private static int maxStackTraceElements = 30;

	/**
	 * Advice enabled flag. If false the interception proccess will not continue. See
	 * {@link #prepareIntercept(Class, Object, Method, Object...)}
	 */
	@RemoraConfig.Configurable
	public boolean enabled = true;

	/**
	 * Filter list applied for Advice.
	 */
	@RemoraConfig.Configurable
	public List<AdviceFilter> filters = new ArrayList<>(10);

	/**
	 * LogLevel for advice.
	 */
	@RemoraConfig.Configurable
	public Level logLevel = Level.OFF;

	/**
	 * Exclude collected properties for advice. See {@link EntryDefinition#addProperty(String, String)}
	 */
	@RemoraConfig.Configurable
	public List<String> excludeProperties = new ArrayList<>(10);

	/**
	 * If flag set, particualr advice will not start {@link CallStack}, thus not correlating particular events. This is
	 * needed for some "low level" interceptions, i.e. Socket or Stereams interception, witch can false-correlate
	 * "higher level", i.e. HTTP, events.
	 */
	@RemoraConfig.Configurable
	public boolean doNotCorrelate = false;

	/**
	 * ThreadLocal there {@link CallStack} is stored.
	 */
	public static ThreadLocal<CallStack> stackThreadLocal = new ThreadLocal<>();

	/**
	 * Adds property if the key already exists with _1, if enabled this will check other is not the same value.
	 * Othervise no check will occur and the pproperty is added to {@link EntryDefinition} despite previos is the same.
	 */
	@RemoraConfig.Configurable
	public static boolean checkCallRepeats = true;

	private final static AgentBuilder agentBuilder = new AgentBuilder.Default(
			new ByteBuddy().ignore(new LatentMatcher.Resolved<>(isSynthetic()))//
					.with(TypeValidation.DISABLED)//
					.with(MethodGraph.Compiler.ForDeclaredMethods.INSTANCE));

	/**
	 * Listeners storage.
	 */
	public List<AdviceListener> listeners = new ArrayList<>(5);

	/**
	 * Effective logger
	 */
	public TaggedLogger logger;

	/**
	 * List for objects there one interception depends on another, i.e. OPEN-CLOSE relationship. The onject reference is
	 * stored and these methods will be correlated if occured in the same instance.
	 */
	private static WeakHashMap<Object, EntryDefinition> trackedObjects = new WeakHashMap<>(1000);

	// TODO not sure if the clenups of this is needed
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

	/**
	 * Gets the byteBudy transform.
	 *
	 * @return
	 */
	public AgentBuilder.Identified.Extendable getTransform() {
		AgentBuilder.Transformer noop = new AgentBuilder.Transformer() {
			@Override
			public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder, TypeDescription typeDescription,
					ClassLoader classLoader, JavaModule module) {
				return builder;
			}
		};
		AgentBuilder.Transformer java15safe = new AgentBuilder.Transformer() {
			@Override
			public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder, TypeDescription typeDescription,
					ClassLoader classLoader, JavaModule module) {
				return builder.visit(TypeConstantAdjustment.INSTANCE);
			}
		};
		AgentBuilder.Identified.Extendable type = agentBuilder//
				// .with(listener) //
				.disableClassFormatChanges()//
				// .enableUnsafeBootstrapInjection() //
				.ignore(getClassIgnores()) //
				.type(getTypeMatcher())//
				.transform(this.java15safe ? java15safe : noop).transform(getAdvice());

		return type;
	}

	/**
	 * To be ovveriden with actual Advice's class to instercept.
	 *
	 * @return
	 */
	public abstract ElementMatcher<TypeDescription> getTypeMatcher();

	public abstract AgentBuilder.Transformer getAdvice();

	public static void fillDefaultValuesAfter(EntryDefinition entryDefinition, long startTime,
			@Advice.Thrown Throwable exception, InterceptionContext ctx) {
		if (entryDefinition.isChained()) {
			entryDefinition.setChained(false);
			return;
		}
		double duration = ((double) System.nanoTime() - startTime) / (double) TimeUnit.MICROSECONDS.toNanos(1L);
		invokeOnMethodFinished(ctx.interceptorInstance, duration);

		entryDefinition.setDuration((long) duration);

		entryDefinition.stop();

		if (exception != null) {
			handleInstrumentedMethodException(entryDefinition, exception, ctx);
		}

		Stack<EntryDefinition> entryDefinitionStack = stackThreadLocal.get();
		if (entryDefinitionStack != null && entryDefinitionStack.size() >= 2) {
			EntryDefinition lastEntryDefinition = entryDefinitionStack.get(entryDefinitionStack.size() - 2);
			if (lastEntryDefinition != null) {
				entryDefinition.addProperty("PARENT", lastEntryDefinition.getId());
			}
		}
		if (!entryDefinition.isTransparent() && !(stackThreadLocal.get() instanceof EmptyStack)) {
			ctx.interceptorInstance.getLogger().debug("Sending {},  {}", ctx, entryDefinition,
					entryDefinition.isFinished() ? "Finished" : "Not Finished");
			OutputManager.send(entryDefinition);
		}
	}

	public static void handleInstrumentedMethodException(EntryDefinition entryDefinition, Throwable exception,
			InterceptionContext ctx) {
		entryDefinition.setException(exception);

		TaggedLogger logger = ctx.interceptorInstance.getLogger();
		if (logger != null) {
			logger.info("Exception {} occurred in method {}", ctx.interceptorInstance, exception.getMessage(),
					entryDefinition.getClazz());
		}
	}

	public static long fillDefaultValuesBefore(EntryDefinition entryDefinition, ThreadLocal<CallStack> stackThreadLocal,
			Object thiz, Method method, InterceptionContext ctx) {
		BaseTransformers interceptorInstance = ctx.interceptorInstance;
		TaggedLogger logger = interceptorInstance.getLogger();
		if (entryDefinition.isChained()) {
			logger.debug("Ed {} chained, not filling in", ctx, entryDefinition);
			return 0;
		}

		try {
			if (thiz != null) {
				entryDefinition.setClazz(thiz.getClass().getName());
			} else {
				if (logger != null) {
					logger.error("\"This\" not filled", ctx.interceptorInstance);
				}
			}

			if (method != null) {
				entryDefinition.setName(method.getName());
			} else {
				if (logger != null) {
					logger.info("#Method not filled", ctx.interceptorInstance);
				}
			}

			if (interceptorInstance.getClass().isAnnotationPresent(Tracked.class)) {
				trackedObjects.put(thiz, entryDefinition);
			}

			if (stackThreadLocal != null && !interceptorInstance.doNotCorrelate) {
				if (stackThreadLocal.get() == null) {
					CallStack definitions = new CallStack(ctx, callStackLimit);
					stackThreadLocal.set(definitions);
				}
				stackThreadLocal.get().push(entryDefinition);
			} else {
				logger.debug("StackThreadLocal null = {}, DoNotCorrelate = {}", ctx, stackThreadLocal == null,
						interceptorInstance.doNotCorrelate);
			}

			entryDefinition.setThread(Thread.currentThread().toString());
			entryDefinition.setStartTime(System.currentTimeMillis());
			if (getAdviceInstance(entryDefinition.getAdviceClassClass()).sendStackTrace) {
				entryDefinition.setStackTrace(getStackTrace());
			}
			if (!entryDefinition.isTransparent() || !(stackThreadLocal.get() instanceof EmptyStack)) {
				logger.debug("Sending {},  {}", ctx, entryDefinition,
						entryDefinition.isFinished() ? "Finished" : "Not Finished");
				OutputManager.send(entryDefinition);
			} else {
				logger.debug("Transparent = {}, EmptyStack = {}", ctx, !(stackThreadLocal.get() instanceof EmptyStack));
			}

		} catch (Throwable t) {
			if (logger != null) {
				logger.error(t, "####Advice error/fillDefaultValuesBefore: {}", ctx.interceptorInstance, t);
			}
		}
		return System.nanoTime();
	}

	public static void untrack(Object thiz, EntryDefinition entryDefinition) {
		EntryDefinition remove = trackedObjects.remove(thiz);
		if (remove != null) {
			entryDefinition.setCorrelator(remove.getId());
		}
	}

	private static void invokeOnIntercept(BaseTransformers adviceInstance, Object thiz, Method method) {

		List<AdviceListener> listeners = adviceInstance.listeners;
		for (AdviceListener listener : listeners) {
			listener.onIntercept(adviceInstance, thiz, method);
		}

	}

	private static void invokeOnProcessed(BaseTransformers adviceInstance, Object thiz, Method method) {
		List<AdviceListener> listeners = adviceInstance.listeners;
		for (AdviceListener listener : listeners) {
			listener.onProcessed(adviceInstance, thiz, method);
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

	private static void invokeOnError(BaseTransformers adviceInstance, Throwable t) {
		List<AdviceListener> listeners = adviceInstance.listeners;
		for (AdviceListener listener : listeners) {
			listener.onAdviceError(adviceInstance, t);
		}

	}

	private static void invokeOnMethodFinished(BaseTransformers adviceClass, double elapseTime) {

		for (AdviceListener listener : adviceClass.listeners) {
			listener.onMethodFinished(adviceClass, elapseTime);
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

	public static void doFinally(InterceptionContext ctx, Class<?> caller) {
		TaggedLogger logger = ctx.interceptorInstance.getLogger();
		if (logger != null) {
			logger.debug("Finalizing {} interception", ctx.interceptorInstance, caller.getSimpleName());
		}
		try {

			if (stackThreadLocal != null) {
				Stack<EntryDefinition> entryDefinitions = stackThreadLocal.get();
				if (entryDefinitions != null) {
					EntryDefinition peek = entryDefinitions.peek();
					if (peek != null && caller != null) {

						if (Objects.equals(peek.getClazz(), caller.getName())
								&& !ctx.interceptorInstance.doNotCorrelate) {
							entryDefinitions.pop();
						} else {
							logger.debug("Entry not popped from stack. Reason {}!={}, doNotCorrelate={}",
									ctx.interceptorInstance, peek.getClazz(), caller.getName(),
									ctx.interceptorInstance.doNotCorrelate);
						}

					}
					if (entryDefinitions.size() <= 0) {
						stackThreadLocal.remove();
						if (logger != null) {
							logger.info("Stack end {}.", ctx.interceptorInstance, peek.getId());
						}
					}
				}
			} else {
				if (logger != null) {
					logger.error("No CallStack", ctx.interceptorInstance);
				}
			}
		} catch (Exception e) {
			if (logger != null) {
				logger.error(e);
			}
		}
	}

	public static boolean isChainedClassInterception(Class<?> adviceClass, TaggedLogger logger, EntryDefinition lastED,
			InterceptionContext ctx) {
		if (lastED == null) {
			return false;
		}
		try {
			if (adviceClass.getSimpleName().equals(lastED.getAdviceClass())) {
				if (logger != null) {
					logger.debug("Stack contains the same advice", ctx.interceptorInstance);
				}
				return true;
			}
		} catch (Exception e) {
			if (logger != null) {
				logger.info(e, "Can't check if advice stack has stacked common advices", ctx.interceptorInstance);
			}
		}
		return false;
	}

	public static void handleAdviceException(Throwable t, InterceptionContext ctx) {
		BaseTransformers adviceInstance = ctx.interceptorInstance;
		TaggedLogger logger = adviceInstance.getLogger();
		invokeOnError(adviceInstance, t);

		if (logger != null) {
			logger.error(t, "{} threw an exception {} {} intercepting method = {}", ctx.interceptorInstance,
					adviceInstance, adviceInstance.getClass(), t.getMessage(), t.getClass().getName(), ctx.method);
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

	public static InterceptionContext prepareIntercept(Class<? extends BaseTransformers> tClass, Object thiz,
			Method method, Object... arguments) {
		BaseTransformers adviceInstance = getAdviceInstance(tClass);
		InterceptionContext context = new InterceptionContext(adviceInstance, method);

		context.interceptorInstance = adviceInstance;
		invokeOnIntercept(adviceInstance, thiz, method);
		if (stackThreadLocal.get() instanceof EmptyStack) {
			// context.intercept = false;
			return context;
		}
		if (!adviceInstance.enabled) {
			// context.intercept = false;
			return context;
		}
		for (AdviceFilter filter : adviceInstance.filters) {
			if (!filter.intercept(thiz, method, arguments)) {
				if (filter.excludeWholeStack()) {
					if (stackThreadLocal.get() == null || stackThreadLocal.get().isEmpty()) {
						stackThreadLocal.set(new EmptyStack(context, callStackLimit));
					}
				}
				// context.intercept = false;
				return context;
			}
		}
		invokeOnProcessed(adviceInstance, thiz, method);
		context.intercept = true;
		return context;
	}

	public static class InterceptionContext {
		public boolean intercept;
		public EntryDefinition ed;
		public BaseTransformers interceptorInstance;
		public Method method;

		public InterceptionContext(BaseTransformers adviceInstance, Method m) {
			interceptorInstance = adviceInstance;
			method = m;
		}
	}

	protected AgentBuilder.Listener getListener() {
		return new TransformationLoggingListener(logger);
	}

	@Override

	public void install(Instrumentation instrumentation) {
		logger = Logger.tag(getName());
		if (load) {
			getTransform().with(getListener()).installOn(instrumentation);
		} else {
			logger.info("Advice {} not enabled", this, getName());
		}
	}

	public static String format(String pattern, Object... args) {
		return MessageFormat.format(pattern, args);
	}

	public static EntryDefinition getEntryDefinition(EntryDefinition ed, Class<? extends BaseTransformers> adviceClass,
			InterceptionContext ctx) {
		if (ed != null) {
			return ed;
		}

		TaggedLogger logger = ctx.interceptorInstance.getLogger();
		EntryDefinition lastED = null;
		CallStack entryDefinitions = stackThreadLocal.get();
		if (entryDefinitions != null && !entryDefinitions.isEmpty()) {
			lastED = entryDefinitions.peek();
		}
		if (adviceClass.isAnnotationPresent(TransparentAdvice.class)) {
			if (lastED != null && lastED.isTransparent()) {
				if (logger != null) {
					logger.debug("Transparent advice, last ED is transparent, returning last {}",
							ctx.interceptorInstance, lastED.getId());
				}
				return lastED;
			} else {

				EntryDefinition entryDefinition = new EntryDefinition(adviceClass, checkCallRepeats);
				invokeEventCreate(adviceClass, entryDefinition);
				if (logger != null) {
					logger.debug("Transparent advice, no previous transparent advice, returning new {}",
							ctx.interceptorInstance, entryDefinition.getId());
				}
				entryDefinition.setTransparent();
				entryDefinition.setMode(EntryDefinition.Mode.STOP);
				return entryDefinition;
			}

		} else {
			if (lastED != null && lastED.isTransparent()) {
				if (logger != null) {
					logger.debug("Nontransparent advice, previous transparent advice, returning last {}",
							ctx.interceptorInstance, lastED.getId());
				}
				lastED.setAdviceClass(adviceClass);
				lastED.setTransparent(false);
				lastED.setMode(EntryDefinition.Mode.RUNNING);
				return lastED;
			} else {

				if (isChainedClassInterception(adviceClass, logger, lastED, ctx)) {
					lastED.setChained();
					if (logger != null) {
						logger.debug(
								"Nontransparent advice, previous non transparent advice, chained, returning last {}",
								ctx.interceptorInstance, lastED.getId());
					}
					return lastED;
				} else {

					EntryDefinition entryDefinition = new EntryDefinition(adviceClass, checkCallRepeats);
					invokeEventCreate(adviceClass, entryDefinition);
					if (logger != null) {
						logger.debug("Nontransparent advice, previous non transparent advice, returning new {}",
								ctx.interceptorInstance, entryDefinition.getId());
					}
					return entryDefinition;
				}
			}
		}
	}

	public static boolean checkEntryDefinition(EntryDefinition ed, InterceptionContext ctx) {
		if (ed == null) { // ed expected to be null if not created by entry, that's for duplicates
			ctx.interceptorInstance.logger.error(
					"EntryDefinition is null, entry might be filtered out as duplicate or ran on test",
					ctx.interceptorInstance);
			return false;
		} else {
			return true;
		}
	}

	@Override
	public Level getLogLevel() {
		return logLevel;
	}

	@Override
	public TaggedLogger getLogger() {
		return logger;
	}

}
