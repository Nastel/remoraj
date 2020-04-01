/*
 *
 * Copyright (c) 2019-2020 NasTel Technologies, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of NasTel
 * Technologies, Inc. ("Confidential Information").  You shall not disclose
 * such Confidential Information and shall use it only in accordance with
 * the terms of the license agreement you entered into with NasTel
 * Technologies.
 *
 * NASTEL MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF
 * THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE, OR NON-INFRINGEMENT. NASTEL SHALL NOT BE LIABLE FOR ANY DAMAGES
 * SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
 * THIS SOFTWARE OR ITS DERIVATIVES.
 *
 * CopyrightVersion 1.0
 */

package com.jkoolcloud.remora.core;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

import com.jkoolcloud.remora.advices.TransparentAdvice;

public class EntryDefinition implements EntryDefinitionDescription {
	protected final String id = JUGFactoryImpl.newUUID();
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

	public EntryDefinition(EntryDefinitionDescription writer2) {

		System.out.println("#####################################");
	}

	public EntryDefinition(Class<?> adviceClass, boolean checkLastPropertyValue) {
		entry.id = id;
		exit.id = id;
		entry.adviceClass = adviceClass.getSimpleName();
		entry.vmIdentification = vmIdentificationStatic;
		this.checkLastPropertyValue = checkLastPropertyValue;
		if (adviceClass.isAnnotationPresent(TransparentAdvice.class)) {
			setTransparent();
		}
	}

	public void setThread(String thread) {
		entry.thread = thread;
	}

	public Map<String, String> getProperties() {
		return exit.properties;
	}

	public void addProperty(String key, String value) {
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
			return ((EntryDefinition) o).getId().equals(getId());
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		return "EntryDefinition{" + "id='" + id + '\'' + ", advice=" + entry.adviceClass + '}';
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

	public enum EventType {
		CALL, SEND, RECEIVE, OPEN, CLOSE
	}

	public enum Mode {
		RUNNING, STOP, EXCEPTION
	}

	public enum ResourceType {
		GENERIC, USER, APPL, PROCESS, APPSERVER, SERVER, RUNTIME, VIRTUAL, NETWORK, DEVICE, NETADDR, GEOADDR, DATACENTER, DATASTORE, CACHE, SERVICE, QUEUE, FILE, TOPIC, DATABASE, HTTP
	}
}
