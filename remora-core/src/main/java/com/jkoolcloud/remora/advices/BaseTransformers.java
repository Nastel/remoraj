package com.jkoolcloud.remora.advices;

import static net.bytebuddy.matcher.ElementMatchers.nameStartsWith;
import static net.bytebuddy.matcher.ElementMatchers.none;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.TimeUnit;

import com.jkoolcloud.remora.RemoraConfig;
import com.jkoolcloud.remora.core.CallStack;
import com.jkoolcloud.remora.core.EntryDefinition;
import com.jkoolcloud.remora.core.JUGFactoryImpl;
import com.jkoolcloud.remora.core.output.OutputManager;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.NamedElement;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

public abstract class BaseTransformers implements RemoraAdvice {

	@RemoraConfig.Configurable
	public static List<String> ignores;

	public static ThreadLocal<Stack<EntryDefinition>> stackThreadLocal = new ThreadLocal<>();

	private static AgentBuilder.Listener.StreamWriting listener = AgentBuilder.Listener.StreamWriting.toSystemError();

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
			@Advice.Thrown Throwable exception) {
		double duration = ((double) System.nanoTime() - startTime) / (double) TimeUnit.MICROSECONDS.toNanos(1L);
		entryDefinition.setDuration((long) duration);

		if (exception == null) {
			entryDefinition.stop();
		} else {
			handleInstrumentedMethodException(entryDefinition, exception);
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

	public static void handleInstrumentedMethodException(EntryDefinition entryDefinition,
			@Advice.Thrown Throwable exception) {
		System.out.println("Exception Occurred!!");
		StringWriter stringWriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(stringWriter);
		exception.printStackTrace(printWriter);
		entryDefinition.setException(exception.getMessage());
		entryDefinition.setExceptionTrace(stringWriter.toString());
	}

	public static long fillDefaultValuesBefore(EntryDefinition entryDefinition,
			ThreadLocal<Stack<EntryDefinition>> stackThreadLocal, @Advice.This Object thiz,
			@Advice.Origin Method method) {
		try {
			if (thiz != null) {
				entryDefinition.setClazz(thiz.getClass().getName());
			} else {
				System.out.println("#This not filled");
			}

			if (method != null) {
				entryDefinition.setName(method.getName());
			} else {
				System.out.println("#Method not filled");
			}

			if (stackThreadLocal != null && stackThreadLocal.get() == null) {
				Stack<EntryDefinition> definitions = new CallStack<EntryDefinition>();
				stackThreadLocal.set(definitions);
				entryDefinition.setCorrelator(new JUGFactoryImpl().newUUID());
			}

			stackThreadLocal.get().push(entryDefinition);
			entryDefinition.setCorrelator(stackThreadLocal.get().get(0).getCorrelator());
			entryDefinition.setThread(Thread.currentThread().toString());
			entryDefinition.setStartTime(System.currentTimeMillis());
			entryDefinition.setStackTrace(getStackTrace());
			OutputManager.INSTANCE.send(entryDefinition);
		} catch (Throwable t) {
			System.out.println("####Advice error/common");
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

	public static boolean isChainedClassInterception(Class<?> adviceClass) {
		try {
			if (adviceClass.equals(stackThreadLocal.get().peek().getAdviceClass())) {
				System.out.println("Stack contains the same advice");
				return true;
			}
		} catch (Exception e) {
			System.out.println("cant check");
		}
		return false;
	}

	public static void handleAdviceException(Throwable t, String adviceName) {
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
		for (String ignore : ignores) {
			ad = ad.or(nameStartsWith(ignore));
		}
		return ad;
	}

	private AgentBuilder.Listener getListener() {
		return AgentBuilder.Listener.StreamWriting.toSystemOut().withTransformationsOnly();
	}

	@Override
	public void install(Instrumentation inst) {
		getTransform().with(getListener()).installOn(inst);
	}

}
