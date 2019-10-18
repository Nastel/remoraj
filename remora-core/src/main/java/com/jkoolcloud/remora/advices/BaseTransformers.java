package com.jkoolcloud.remora.advices;

import static com.jkoolcloud.remora.Remora.LOG_COUNT;
import static com.jkoolcloud.remora.Remora.LOG_FILE_SIZE;
import static net.bytebuddy.matcher.ElementMatchers.nameStartsWith;
import static net.bytebuddy.matcher.ElementMatchers.none;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.TimeUnit;
import java.util.logging.*;

import com.jkoolcloud.remora.Remora;
import com.jkoolcloud.remora.RemoraConfig;
import com.jkoolcloud.remora.core.CallStack;
import com.jkoolcloud.remora.core.EntryDefinition;
import com.jkoolcloud.remora.core.JUGFactoryImpl;
import com.jkoolcloud.remora.core.output.OutputManager;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.NamedElement;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.utility.JavaModule;

public abstract class BaseTransformers implements RemoraAdvice {

	@RemoraConfig.Configurable
	public static List<String> ignores;

	public static ThreadLocal<Stack<EntryDefinition>> stackThreadLocal = new ThreadLocal<>();

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
		return new AgentBuilder.Default()//
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
			@Advice.Thrown Throwable exception, Logger logger) {
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
		OutputManager.INSTANCE.send(entryDefinition);
	}

	public static void handleInstrumentedMethodException(EntryDefinition entryDefinition, Throwable exception,
			Logger logger) {
		StringWriter stringWriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(stringWriter);
		exception.printStackTrace(printWriter);
		entryDefinition.setException(exception.getMessage());
		entryDefinition.setExceptionTrace(stringWriter.toString());

		if (logger != null) {
			logger.info(
					format("Exception {0} occurred in method {1}", exception.getMessage(), entryDefinition.getClazz()));
		}
	}

	public static long fillDefaultValuesBefore(EntryDefinition entryDefinition,
			ThreadLocal<Stack<EntryDefinition>> stackThreadLocal, Object thiz, Method method, Logger logger) {
		try {
			if (thiz != null) {
				entryDefinition.setClazz(thiz.getClass().getName());
			} else {
				logger.info("This not filled");
			}

			if (method != null) {
				entryDefinition.setName(method.getName());
			} else {
				logger.info("#Method not filled");
			}

			if (stackThreadLocal != null && stackThreadLocal.get() == null) {
				Stack<EntryDefinition> definitions = new CallStack<EntryDefinition>(logger);
				stackThreadLocal.set(definitions);
				String correlator = new JUGFactoryImpl().newUUID();
				entryDefinition.setCorrelator(correlator);
				if (logger != null) {
					logger.info(format("#New stack correlator {0}", correlator));
				}
			}

			stackThreadLocal.get().push(entryDefinition);
			entryDefinition.setCorrelator(stackThreadLocal.get().get(0).getCorrelator());
			entryDefinition.setThread(Thread.currentThread().toString());
			entryDefinition.setStartTime(System.currentTimeMillis());
			entryDefinition.setStackTrace(getStackTrace());
			OutputManager.INSTANCE.send(entryDefinition);
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
		sb.append("\n");
		for (StackTraceElement element : stackTrace) {
			i++;
			if (i >= MAX_ELEMENTS) {
				break;
			}

			sb.append(element.getClassName());
			sb.append(".");
			sb.append(element.getMethodName());
			sb.append("()");
			sb.append("\n");

		}
		return sb.toString();
	}

	public static void doFinally() {
		if (stackThreadLocal != null) {
			Stack<EntryDefinition> entryDefinitions = stackThreadLocal.get();
			if (entryDefinitions != null) {
				entryDefinitions.pop();
				if (entryDefinitions.size() == 0) {
					stackThreadLocal.remove();
				}
			}
		}
	}

	public static boolean isChainedClassInterception(Class<?> adviceClass, Logger logger) {
		try {
			if (adviceClass.equals(stackThreadLocal.get().peek().getAdviceClass())) {
				if (logger != null) {
					logger.info(("Stack contains the same advice"));
				}
				return true;
			}
		} catch (Exception e) {
			logger.info(("Can't check if advice stack has stacked common advices"));
		}
		return false;
	}

	public static void handleAdviceException(Throwable t, String adviceName, Logger logger) {
		BaseTransformers.class.getSimpleName();
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
		}
		return ad;
	}

	protected abstract AgentBuilder.Listener getListener();

	@Override
	public void install(Instrumentation inst) {
		getTransform().with(getListener()).installOn(inst);
	}

	protected static void configureAdviceLogger(Logger logger) {
		FileHandler handler = null;
		try {
			String path = System.getProperty(Remora.REMORA_PATH) + "/log/";
			new File(path).mkdirs();
			String pattern = path + logger.getName() + "%g%u%u%u.log";
			handler = new FileHandler(pattern, LOG_FILE_SIZE, LOG_COUNT, true);
			handler.setFilter(new PassAllFilter());
			handler.setFormatter(Remora.REMORA_LOG_FORMATTER);
			handler.setLevel(Level.ALL);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (logger != null) {
			Arrays.asList(logger.getHandlers()).stream().forEach(l -> logger.removeHandler(l));
			logger.addHandler(handler);
			logger.addHandler(new StreamHandler(System.out, Remora.REMORA_LOG_FORMATTER));

			logger.setUseParentHandlers(false);
			logger.setLevel(Level.parse(
					RemoraConfig.INSTANCE.config.getProperty(logger.getName() + "logLevel", Level.FINEST.getName())));

			logger.info(format("Advice logger configured, level {1}, handlers {2},  ", logger, logger.getLevel(),
					logger.getHandlers()));
			logger.severe("SEVERE is displayed");
			logger.warning("WARNING is displayed");
			logger.config("CONFIG is displayed");
			logger.info("INFO is displayed");
			logger.info("FINE is displayed");
			logger.finer("FINER is displayed");
			logger.finer("FINEST is displayed");

		}
	}

	public static String format(String pattern, Object... args) {
		return MessageFormat.format(pattern, args);
	}

	private static class PassAllFilter implements Filter {

		@Override
		public boolean isLoggable(LogRecord record) {
			return true;
		}

	}

	public static class TransformationLoggingListener extends AgentBuilder.Listener.Adapter {
		Logger logger;
		public final static String PREFIX = "[ByteBuddy]";

		public TransformationLoggingListener(Logger logger) {
			this.logger = logger;
		}

		@Override
		public void onTransformation(TypeDescription typeDescription, ClassLoader classLoader, JavaModule module,
				boolean loaded, DynamicType dynamicType) {
			System.out.println(format(PREFIX + " TRANSFORM {0} [{1}, {2}, loaded={3}]", typeDescription.getName(),
					classLoader, module, loaded));
			logger.info(format(PREFIX + " TRANSFORM {0} [{1}, {2}, loaded={3}]", typeDescription.getName(), classLoader,
					module, loaded));
		}

		@Override
		public void onError(String typeName, ClassLoader classLoader, JavaModule module, boolean loaded,
				Throwable throwable) {

			System.out.println(
					format(PREFIX + " ERROR {0} [{1}, {2}, loaded={3}] \n", typeName, classLoader, module, loaded));
			logger.info(format(PREFIX + " ERROR {0} [{1}, {2}, loaded={3}] \n", typeName, classLoader, module, loaded));
			logger.info(Arrays.toString(throwable.getStackTrace()));
			throwable.printStackTrace();
		}

	}

}
