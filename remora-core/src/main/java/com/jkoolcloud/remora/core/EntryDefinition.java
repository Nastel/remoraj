package com.jkoolcloud.remora.core;

import java.util.HashMap;
import java.util.Map;

import net.openhft.chronicle.wire.AbstractMarshallable;

public class EntryDefinition extends AbstractMarshallable {
	final String id = new JUGFactoryImpl().newUUID();
	final String adviceClass;
	String name;

	String clazz;
	Map<String, String> properties = new HashMap<>();
	String application;

	EventType eventType = EventType.CALL;
	Mode mode = Mode.RUNNING;
	String resource;

	String returnType;
	String returnValue;
	String exception;
	String correlator;
	Long startTime;
	String StackTrace;

	String exceptionTrace;

	public EntryDefinition(Class adviceClass) {
		this.adviceClass = adviceClass.getSimpleName();
	}

	public EntryDefinition() {
        adviceClass = null;
	}

	public void setThread(String thread) {
		this.thread = thread;
	}

	String thread;
	Long duration;

	public Map<String, String> getProperties() {
		return properties;
	}

	public void addProperty(String key, String value) {
		properties.put(key, value);
	}

	public void addProperties(Map map) {
		properties.putAll(map);
	}

	public void addPropertyIfExist(String key, String value) {
		if (value != null) {
			addProperty(key, value);
		}
	}

	public String getClazz() {
		return clazz;
	}

	public void setStartTime(Long startTime) {
		this.startTime = startTime;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setClazz(String clazz) {
		this.clazz = clazz;
	}

	public void setReturnType(String returnType) {
		this.returnType = returnType;
	}

	public void setReturnValue(String returnValue) {
		this.returnValue = returnValue;
	}

	public void setException(String exception) {
        mode = Mode.EXCEPTION;
		this.exception = exception;
	}

	public void setStackTrace(String stackTrace) {
		StackTrace = stackTrace;
	}

	public void setExceptionTrace(String exceptionTrace) {
		this.exceptionTrace = exceptionTrace;
	}

	public Long getDuration() {
		return duration;
	}

	public void setDuration(Long duration) {
		this.duration = duration;
	}

	public void setCorrelator(String correlator) {
		this.correlator = correlator;
	}

	public String getCorrelator() {
		return correlator;
	}

	public String getAdviceClass() {
		return adviceClass;
	}

	public String getId() {
		return id;
	}

	public void setEventType(EventType eventType) {
		this.eventType = eventType;
	}

	public void setEventType(int eventType) {
		try {
			this.eventType = EventType.values()[eventType];
		} catch (Exception e) {
			this.eventType = EventType.CALL;
		}
	}

	public void stop() {
		mode = Mode.STOP;
	}

	public enum EventType {
		CALL, SEND, RECEIVE, OPEN, CLOSE
	}

	public enum Mode {
		RUNNING, STOP, EXCEPTION
	}
}
