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

public class EntryDefinition implements EntryDefinitionDescription {
	private static final String DEFAULT_APPL_NAME = "Java";
	protected final String id = JUGFactoryImpl.newUUID();
	private final Class<? extends BaseTransformers> adviceClass;
	public boolean sentEntry;
	private boolean transparent;
	private boolean chained;
	public static String vmIdentificationStatic;

	public Entry entry = new Entry();
	public Exit exit = new Exit();

	private boolean finished;
	private boolean checkLastPropertyValue;

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

	public void setThread(String thread) {
		entry.thread = thread;
	}

	public Map<String, String> getProperties() {
		return exit.properties;
	}

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

	public void addProperties(Map<Object, Object> map) {
		for (Map.Entry<Object, Object> e : map.entrySet()) {
			addProperty(String.valueOf(e.getKey()), String.valueOf(e.getValue()));
		}

	}

	public void addPropertyIfExist(String key, String value) {
		if (value != null) {
			addProperty(key, value);
		}
	}

	public void addPropertiesIfExist(Map<String, ?> value) {
		for (Map.Entry<String, ?> ve : value.entrySet()) {
			addProperty(ve.getKey(), String.valueOf(ve.getValue()));
		}
	}

	public void addPropertyIfExist(String key, Boolean value) {
		if (value != null) {
			addProperty(key, value.toString());
		}
	}

	public void addPropertyIfExist(String key, Number value) {
		if (value != null) {
			addProperty(key, value.toString());
		}
	}

	public String getClazz() {
		return entry.clazz;
	}

	public void setStartTime(Long startTime) {
		entry.startTime = startTime;
	}

	public long getStartTime() {
		return entry.startTime;
	}

	public void setName(String name) {
		entry.name = name;
		exit.name = name;
	}

	public void setClazz(String clazz) {
		if (clazz != null) {
			addProperty("SCLASS", clazz);
		}
		entry.clazz = clazz;
	}

	public void setAdviceClass(Class<?> adviceClass) {
		entry.adviceClass = adviceClass.getSimpleName();
	}

	public void setException(String exception) {
		finished = true;
		exit.mode = Mode.EXCEPTION;
		exit.exception = exception;
	}

	public void setStackTrace(String stackTrace) {
		entry.stackTrace = stackTrace;
	}

	public void setExceptionTrace(String exceptionTrace) {
		exit.exceptionTrace = exceptionTrace;
	}

	public void setDuration(Long duration) {
		exit.duration = duration;
	}

	public void setCorrelator(String correlator) {
		exit.correlator = correlator;
	}

	public String getCorrelator() {
		return exit.correlator;
	}

	public String getAdviceClass() {
		return entry.adviceClass;
	}

	public String getId() {
		return id;
	}

	public void setEventType(EventType eventType) {
		exit.eventType = eventType;
	}

	public void setEventType(int eventType) {
		try {
			exit.eventType = EventType.values()[eventType];
		} catch (Exception e) {
			exit.eventType = EventType.CALL;
		}
	}

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

	public boolean isTransparent() {
		return transparent;
	}

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

	public void setTransparent() {
		transparent = true;
	}

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

	public enum EventType {
		CALL, SEND, RECEIVE, OPEN, CLOSE
	}

	public enum Mode {
		RUNNING, STOP, EXCEPTION
	}

	public enum ResourceType {
		GENERIC, USER, APPL, PROCESS, APPSERVER, SERVER, RUNTIME, VIRTUAL, NETWORK, DEVICE, NETADDR, GEOADDR, DATACENTER, DATASTORE, CACHE, SERVICE, QUEUE, FILE, TOPIC, DATABASE, HTTP
	}

	public static class DummyEntryDefinition extends EntryDefinition {

		public DummyEntryDefinition() {
			super(BaseTransformers.class, false);
		}
	}
}
