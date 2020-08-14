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

package com.jkoolcloud.remora.core;

import static java.text.MessageFormat.format;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

import com.jkoolcloud.remora.AdviceRegistry;
import com.jkoolcloud.remora.advices.BaseTransformers;
import com.jkoolcloud.remora.advices.RemoraAdvice;
import com.jkoolcloud.remora.advices.TransparentAdvice;

/**
 * This class is supposed to store the instrumentation data eventually to be passed to the
 * {@link com.jkoolcloud.remora.core.output.AgentOutput}.
 * <p>
 * Actually there is two entities involved: {@link Entry} and {@link Exit}. And {@link EntryDefinition} is convenience
 * wrapper for these two.
 * <p>
 * On a single method interception RemoraJ will output two times - on method enter and on method exit. There is the only
 * way to catch method that newer ended.
 * <p>
 * {@link Entry} and {@link Exit} carry distinct data for an interception.
 * <p>
 * Each Entry definition has unique GUID, created on new instance. This is shared for Entry and Exit.
 */

public class EntryDefinition implements EntryDefinitionDescription {
	private static final String DEFAULT_APPL_NAME = "Java";
	protected final String id = JUGFactoryImpl.newUUID();
	private final Class<? extends BaseTransformers> adviceClass;

	/**
	 * Flags that the entry is already sent. This is marked by {@link com.jkoolcloud.remora.core.output.AgentOutput} to
	 * ensure that entry data is sent, as Output generally sends the data asynchronously.
	 */
	public boolean sentEntry;

	/**
	 * Marks that this entry as transparent, effectively created by Advice annotated with {@link TransparentAdvice} ,
	 * and shouldn't be processed by {@link com.jkoolcloud.remora.core.output.AgentOutput}.
	 */
	private boolean transparent;

	/**
	 * Marks the data that it's processed as chained.
	 */
	private boolean chained;

	/**
	 * Store of vmIdentification, effectively copied to actual instances of {@link EntryDefinition}
	 */
	public static String vmIdentificationStatic;

	/**
	 * Actual entry
	 */
	public Entry entry = new Entry();
	/**
	 * Actual exit
	 */
	public Exit exit = new Exit();

	/**
	 * Flags as finished event. See {@link #stop()}
	 */
	private boolean finished;

	/**
	 * Flags to check property to already known value. See {@link BaseTransformers#checkCallRepeats}
	 */
	private boolean checkLastPropertyValue;

	/**
	 * Default constructor
	 *
	 * @param adviceClass
	 * @param checkLastPropertyValue
	 */

	public EntryDefinition(Class<? extends BaseTransformers> adviceClass, boolean checkLastPropertyValue) {
		entry.id = id;
		exit.id = id;
		exit.application = System.getProperty("remora.appl.name", DEFAULT_APPL_NAME);
		entry.adviceClass = adviceClass.getSimpleName();
		this.adviceClass = adviceClass;
		entry.vmId = vmIdentificationStatic;
		this.checkLastPropertyValue = checkLastPropertyValue;
		if (adviceClass.isAnnotationPresent(TransparentAdvice.class)) {
			transparent = true;
		}
	}

	public boolean isFinished() {
		return finished;
	}

	@Override
	public void entry(Entry entry) {
		this.entry = entry;
	}

	@Override
	public void exit(Exit exit) {
		this.exit = exit;
	}

	public void setThread(String thread) {
		entry.thread = thread;
	}

	/**
	 * @return collected properties.
	 */

	public Map<String, String> getProperties() {
		return exit.properties;
	}

	/**
	 * Adds property to {@link Exit}. No property should be added after {@link BaseTransformers fillDefaultValuesAfter}
	 * as it's effectively sends the entity to {@link com.jkoolcloud.remora.core.output.AgentOutput}, and as it can be
	 * processed asynchronously, the {@link java.util.ConcurrentModificationException} might occur. If the property Map
	 * already contains such a value, the property will be added with key + "_1". If {@link #checkLastPropertyValue}
	 * flag is set the method will also check before adding that it's not the same value as before.
	 *
	 * @param key
	 * @param value
	 */
	public void addProperty(String key, String value) {
		try {
			RemoraAdvice adviceByName = AdviceRegistry.INSTANCE.getBaseTransformerByName(adviceClass.getSimpleName());
			if (((BaseTransformers) adviceByName).excludeProperties.contains(key)) {
				return;
			}
		} catch (ClassNotFoundException e) {
		}
		String lastValue = value;
		int iteration = 0;
		if (checkLastPropertyValue) {
			String lastActualValue = exit.properties.get(key);
			if (lastActualValue == value) {
				return;
			}
		}

		while (lastValue != null) {
			// synchronized (properties) {
			if (iteration == 0) {
				lastValue = exit.properties.put(key, lastValue);
			} else {
				lastValue = exit.properties.put(key + "_" + iteration, lastValue);
			}
			iteration++;
			// }
		}

	}

	/**
	 * Adds multiple properties. See {@link #addProperty(String, String)}.
	 *
	 * @param map
	 */
	public void addProperties(Map<Object, Object> map) {
		for (Map.Entry<Object, Object> e : map.entrySet()) {
			addProperty(String.valueOf(e.getKey()), String.valueOf(e.getValue()));
		}

	}

	/**
	 * Adds property if value not null. See {@link #addProperty(String, String)}.
	 */

	public void addPropertyIfExist(String key, String value) {
		if (value != null) {
			addProperty(key, value);
		}
	}

	/**
	 * Adds multiple properties if value is not null. See {@link #addProperty(String, String)}.
	 *
	 * @param value
	 */
	public void addPropertiesIfExist(Map<String, ?> value) {
		for (Map.Entry<String, ?> ve : value.entrySet()) {
			addProperty(ve.getKey(), String.valueOf(ve.getValue()));
		}
	}

	/**
	 * Adds property if value not null. See {@link #addProperty(String, String)}.
	 */
	public void addPropertyIfExist(String key, Boolean value) {
		if (value != null) {
			addProperty(key, value.toString());
		}
	}

	/**
	 * Adds property if value not null. See {@link #addProperty(String, String)}.
	 */
	public void addPropertyIfExist(String key, Number value) {
		if (value != null) {
			addProperty(key, value.toString());
		}
	}

	/**
	 * Gets the class of intercepted class.
	 *
	 * @return
	 */
	public String getClazz() {
		return entry.clazz;
	}

	/**
	 * Sets the startTime
	 *
	 * @param startTime
	 */
	public void setStartTime(Long startTime) {
		entry.startTime = startTime;
	}

	/**
	 * Gets the startTime
	 *
	 * @return
	 */
	public long getStartTime() {
		return entry.startTime;
	}

	/**
	 * Sets the name for entry. Effectively intercepted method name.
	 *
	 * @param name
	 */

	public void setName(String name) {
		entry.name = name;
		exit.name = name;
	}

	/**
	 * Sets the intercepted class.
	 *
	 * @param clazz
	 */
	public void setClazz(String clazz) {
		if (clazz != null) {
			addProperty("SCLASS", clazz);
		}
		entry.clazz = clazz;
	}

	/**
	 * Set advice's class name.
	 *
	 * @param adviceClass
	 */
	public void setAdviceClass(Class<?> adviceClass) {
		entry.adviceClass = adviceClass.getSimpleName();
	}

	/**
	 * Sets the exception thrown by instrumented method.
	 *
	 * @param exception
	 */
	public void setException(String exception) {
		finished = true;
		exit.mode = Mode.EXCEPTION;
		exit.exception = exception;
	}

	/**
	 * Set's the stack trace for interception. See {@link BaseTransformers#getStackTrace()}.
	 *
	 * @param stackTrace
	 */
	public void setStackTrace(String stackTrace) {
		entry.stackTrace = stackTrace;
	}

	/**
	 * Sets the exception trace.
	 *
	 * @param exceptionTrace
	 */
	public void setExceptionTrace(String exceptionTrace) {
		exit.exceptionTrace = exceptionTrace;
	}

	/**
	 * Sets the method elapsed time
	 *
	 * @param duration
	 */
	public void setDuration(Long duration) {
		exit.duration = duration;
	}

	/**
	 * Sets the correllator for an event. This correlates the same {@link CallStack} events.
	 *
	 * @param correlator
	 */
	public void setCorrelator(String correlator) {
		exit.correlator = correlator;
	}

	/**
	 * Get's the corellator. See {@link #setCorrelator(String)}
	 *
	 * @return
	 */
	public String getCorrelator() {
		return exit.correlator;
	}

	public String getAdviceClass() {
		return entry.adviceClass;
	}

	/**
	 * Get the generated ID. ID are meant to be unique and spread to {@link Entry} and {@link Exit}. See {@link #id}
	 *
	 * @return
	 */
	public String getId() {
		return id;
	}

	/**
	 * Set's the event type. See {@link EventType}
	 *
	 * @param eventType
	 */
	public void setEventType(EventType eventType) {
		exit.eventType = eventType;
	}

	/**
	 * Set's the event type. See {@link EventType}
	 *
	 * @param eventType
	 */
	public void setEventType(int eventType) {
		try {
			exit.eventType = EventType.values()[eventType];
		} catch (Exception e) {
			exit.eventType = EventType.CALL;
		}
	}

	/**
	 * Set's the application for a particular event. If the method is in {@link CallStack} the application is translated
	 * to related {@link EntryDefinition}.
	 */
	public void setApplication(String application) {
		exit.application = application;
	}

	public String getResource() {
		return exit.resource;
	}

	public void setResource(String resource, ResourceType resourceType) {
		exit.resource = resourceType.name() + "=" + resource;
	}

	public void setMode(Mode mode) {
		exit.mode = mode;
	}

	/**
	 * Mark that the interception is finished. See
	 * {@link BaseTransformers#fillDefaultValuesAfter(EntryDefinition, long, Throwable, BaseTransformers.InterceptionContext)}
	 */

	public void stop() {
		finished = true;
		exit.mode = Mode.STOP;
	}

	public void setServer(String server) {
		exit.server = server;
	}

	public String getServer() {
		return exit.server;
	}

	public static void setVmIdentification(String vmIdentification) {
		EntryDefinition.vmIdentificationStatic = vmIdentification;
	}

	public void setException(Throwable exception) {

		StringWriter stringWriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(stringWriter);
		exception.printStackTrace(printWriter);
		setException(exception.getMessage());
		setExceptionTrace(stringWriter.toString());
	}

	/**
	 * Effectively checking the {@link #id} as supposed to be unique.
	 *
	 * @param o
	 * @return
	 */
	@Override
	public boolean equals(Object o) {
		if (o instanceof EntryDefinition) {
			return ((EntryDefinition) o).getId().equals(id);
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		String startTemplate = "RemoraJ entry id={0}, advice={1}, method={2}.{3}()";
		String stopTemplate = "RemoraJ exit id={0}, advice={1}, method={2}.{3}(), time={4,number,#}, exception={5}";
		if (finished) {
			return format(stopTemplate, id, entry.adviceClass, entry.clazz, entry.name, exit.duration, exit.exception);
		} else {
			return format(startTemplate, id, entry.adviceClass, entry.clazz, entry.name);
		}
	}

	@Override
	public int hashCode() {
		return id.getBytes()[0];
	}

	/**
	 * Check that {@link EntryDefinition} is created by advice marked with {@link TransparentAdvice}.
	 *
	 * @return
	 */
	public boolean isTransparent() {
		return transparent;
	}

	/**
	 * Set that {@link EntryDefinition} is created by advice marked with {@link TransparentAdvice}.
	 *
	 * @return
	 */
	public void setTransparent() {
		transparent = true;
	}

	/**
	 * Check that {@link EntryDefinition} is created by advice marked with {@link TransparentAdvice}.
	 *
	 * @return
	 */
	public void setTransparent(boolean b) {
		transparent = b;
	}

	public boolean isChained() {
		return chained;
	}

	public void setChained(boolean chained) {
		this.chained = chained;
	}

	public void setChained() {
		chained = true;
	}

	public Class<? extends BaseTransformers> getAdviceClassClass() {
		return adviceClass;
	}

	/**
	 * Event type for {@link EntryDefinition}
	 */
	public enum EventType {
		CALL, SEND, RECEIVE, OPEN, CLOSE
	}

	/**
	 * Mode
	 */
	public enum Mode {
		/**
		 * Method is marked as entered advice by advice method before() marked
		 * {@link net.bytebuddy.asm.Advice.OnMethodEnter}
		 */
		RUNNING,
		/**
		 * Method is marked as exited advice by advice method after() marked
		 * {@link net.bytebuddy.asm.Advice.OnMethodExit}
		 */
		STOP,
		/**
		 * Method is marked as exited advice by advice method after() marked
		 * {@link net.bytebuddy.asm.Advice.OnMethodExit} and threw an {@link Exception}
		 */
		EXCEPTION
	}

	public enum ResourceType {
		GENERIC, USER, APPL, PROCESS, APPSERVER, SERVER, RUNTIME, VIRTUAL, NETWORK, DEVICE, NETADDR, GEOADDR, DATACENTER, DATASTORE, CACHE, SERVICE, QUEUE, FILE, TOPIC, DATABASE, HTTP
	}

	/**
	 * Marker for EntryDefinition witch first method is filtered out by
	 * {@link com.jkoolcloud.remora.filters.AdviceFilter}
	 */
	public static class DummyEntryDefinition extends EntryDefinition {

		public DummyEntryDefinition() {
			super(BaseTransformers.class, false);
		}
	}
}
