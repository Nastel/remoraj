package com.jkoolcloud.remora.advices;

import static net.bytebuddy.matcher.ElementMatchers.nameStartsWith;
import static net.bytebuddy.matcher.ElementMatchers.none;

import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.TimeUnit;

import org.tinylog.TaggedLogger;

import com.jkoolcloud.remora.RemoraConfig;
import com.jkoolcloud.remora.core.CallStack;
import com.jkoolcloud.remora.core.EntryDefinition;
import com.jkoolcloud.remora.core.output.OutputManager;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.NamedElement;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.scaffold.MethodGraph;
import net.bytebuddy.dynamic.scaffold.TypeValidation;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.utility.JavaModule;

public abstract class BaseTransformers implements RemoraAdvice {

	private static final String ADVICE_NAME = "GENERAL";
	@RemoraConfig.Configurable
	public static List<String> ignores;
	@RemoraConfig.Configurable
	public static boolean sendStackTrace;

	public static ThreadLocal<CallStack<EntryDefinition>> stackThreadLocal = new ThreadLocal<>();

	public static class EnhancedElementMatcher<T extends TypeDescription>
			extends ElementMatcher.Junction.AbstractBase<T> {

		private String[] interceptingClass;

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
		ByteBuddy byteBuddy = new ByteBuddy().with(TypeValidation.DISABLED)
				.with(MethodGraph.Compiler.ForDeclaredMethods.INSTANCE);

		return new AgentBuilder.Default(byteBuddy)//
				// .with(listener) //
				.disableClassFormatChanges()//
				// .enableUnsafeBootstrapInjection() //
				.ignore(getClassIgnores()) //
				.type(getTypeMatcher()) //
				.transform(getAdvice());
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
		entryDefinition.setDuration((long) duration);

		if (exception == null) {
			entryDefinition.stop();
		} else {
			handleInstrumentedMethodException(entryDefinition, exception, logger);
		}

		Stack<EntryDefinition> entryDefinitionStack = stackThreadLocal.get();
		if (entryDefinitionStack != null && entryDefinitionStack.size() >= 2) {
			EntryDefinition lastEntryDefinition = entryDefinitionStack.get(entryDefinitionStack.size() - 2);
			if (lastEntryDefinition != null) {
				entryDefinition.addProperty("PARENT", lastEntryDefinition.getId());
			}
		}
		if (!entryDefinition.isTransparent()) {
			OutputManager.INSTANCE.send(entryDefinition);
		}
	}

	public static void handleInstrumentedMethodException(EntryDefinition entryDefinition, Throwable exception,
			TaggedLogger logger) {
		entryDefinition.setException(exception);

		if (logger != null) {
			logger.info(
					format("Exception {0} occurred in method {1}", exception.getMessage(), entryDefinition.getClazz()));
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

			if (stackThreadLocal != null && stackThreadLocal.get() == null) {
				CallStack<EntryDefinition> definitions = new CallStack<>(logger);
				stackThreadLocal.set(definitions);
			}

			stackThreadLocal.get().push(entryDefinition);
			entryDefinition.setThread(Thread.currentThread().toString());
			entryDefinition.setStartTime(System.currentTimeMillis());
			if (sendStackTrace) {
				entryDefinition.setStackTrace(getStackTrace());
			}
			if (!entryDefinition.isTransparent()) {
				OutputManager.INSTANCE.send(entryDefinition);
			}
		} catch (Throwable t) {
			if (logger != null) {
				logger.info(format("####Advice error/fillDefaultValuesBefore: {0}", t));
			}
		}
		return System.nanoTime();
	}

	public static String getStackTrace() {
		StackTraceElement[] stackTrace = new Exception().getStackTrace();
		int i = 0;
		int MAX_ELEMENTS = 30;
		StringBuilder sb = new StringBuilder();
		sb.append("Stack length: ");
		sb.append(stackTrace.length);
		sb.append("\n ");
		for (StackTraceElement element : stackTrace) {
			i++;
			if (i >= MAX_ELEMENTS) {
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

	public static void doFinally(TaggedLogger logger) {
		if (logger != null) {
			logger.debug("DoFinnaly");
		}
		try {

			if (stackThreadLocal != null) {
				Stack<EntryDefinition> entryDefinitions = stackThreadLocal.get();
				if (entryDefinitions != null) {
					EntryDefinition pop;
					if (entryDefinitions.peek() != null) {
						// boolean notChained = !entryDefinitions.peek().isChained();
						// if (notChained) {
						do {
							pop = entryDefinitions.pop();
						} while (pop.isTransparent());
						// } else {
						// if (logger != null) {
						// logger.info("Not popping ED, chained");
						// }
						// entryDefinitions.peek().setChained(false);

						// }
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
		if (logger != null) {
			logger.info("{0} threw an exception {2} {1}", adviceName, t.getMessage(), t.getClass().getName());
			logger.info(Arrays.toString(t.getStackTrace()));
		}
	}

	protected ElementMatcher<NamedElement> getClassIgnores() {
		return nameStartsWith("net.openhft") //
				.or(nameStartsWith("java.lang")) //
				.or(nameStartsWith("com.jkoolcloud.remora")) //
				.or(nameStartsWith("net.bytebuddy")) //
				.or(getFromConfig());
	}

	private ElementMatcher<NamedElement> getFromConfig() {
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

	protected abstract AgentBuilder.Listener getListener();

	public static String format(String pattern, Object... args) {
		return MessageFormat.format(pattern, args);
	}

	public static EntryDefinition getEntryDefinition(EntryDefinition ed, Class adviceClass, TaggedLogger logger) {
		if (ed != null) {
			return ed;
		}

		EntryDefinition lastED = null;
		CallStack<EntryDefinition> entryDefinitions = stackThreadLocal.get();
		if (entryDefinitions != null && entryDefinitions.size() != 0) {
			lastED = entryDefinitions.peek();
		}
		if (adviceClass.isAnnotationPresent(TransparentAdvice.class)) {
			if (lastED != null && lastED.isTransparent()) {
				if (logger != null) {
					logger.debug("Transparent advice, last ED is transparent, returning last {0}", lastED.getId());
				}
				return lastED;
			} else {

				EntryDefinition entryDefinition = new EntryDefinition(adviceClass);
				if (logger != null) {
					logger.debug("Transparent advice, no previous transparent advice, returning new {0}",
							entryDefinition.getId());
				}
				entryDefinition.setTransparent();
				entryDefinition.setMode(EntryDefinition.Mode.STOP);
				return entryDefinition;
			}

		} else {
			if (lastED != null && lastED.isTransparent()) {
				if (logger != null) {
					logger.debug("Nontransparent advice, previous transparent advice, returning last {0}",
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
								"Nontransparent advice, previous non transparent advice, chained, returning last {0}",
								lastED.getId());
					}
					return lastED;
				} else {

					EntryDefinition entryDefinition = new EntryDefinition(adviceClass);
					if (logger != null) {
						logger.debug("Nontransparent advice, previous non transparent advice, returning new {0}",
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
				System.out.println(format(PREFIX + " TRANSFORM {0} [{1}, {2}, loaded={3}]", typeDescription.getName(),
						classLoader, module, loaded));
			} else {
				logger.info(format(PREFIX + " TRANSFORM {0} [{1}, {2}, loaded={3}]", typeDescription.getName(),
						classLoader, module, typeDescription));

			}
		}

		@Override
		public void onError(String typeName, ClassLoader classLoader, JavaModule module, boolean loaded,
				Throwable throwable) {

			if (logger == null) {
				System.out.println(
						format(PREFIX + " ERROR {0} [{1}, {2}, loaded={3}] \n", typeName, classLoader, module, loaded));
				throwable.printStackTrace();
			} else {
				logger.info(
						format(PREFIX + " ERROR {0} [{1}, {2}, loaded={3}] \n", typeName, classLoader, module, loaded));
				logger.info(throwable.getMessage());
				logger.info(Arrays.toString(throwable.getStackTrace()));
			}

		}

	}

}
