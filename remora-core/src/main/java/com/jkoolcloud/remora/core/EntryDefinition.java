package com.jkoolcloud.remora.core;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import com.jkoolcloud.remora.advices.TransparentAdvice;

import net.openhft.chronicle.wire.AbstractMarshallable;

public class EntryDefinition extends AbstractMarshallable {
	protected final String id = new JUGFactoryImpl().newUUID();

	private boolean transparent;

	private String adviceClass;
	protected String name;
	private String clazz;
	private Map<String, String> properties = new HashMap<>();

	private String application;
	private String server;

    //Workaround for serializatiom, static fields not serialised
	private static String vmIdentificationStatic;
	private String vmIdentification;

	private String resource;
	private ResourceType resourceType;

	private EventType eventType = EventType.CALL;
	private Mode mode = Mode.RUNNING;

	private String returnType;

	private String returnValue;
	protected String exception;
	private String correlator;
	private Long startTime;

	private String StackTrace;
	private String exceptionTrace;

	public EntryDefinition(Class adviceClass) {
		this.adviceClass = adviceClass.getSimpleName();
		vmIdentification = vmIdentificationStatic;
		if (adviceClass.isAnnotationPresent(TransparentAdvice.class)) {
			setTransparent();
		}
	}

	// public no arg constructor needed for serialization, thus marked as @Deprecated
	@Deprecated
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
		String lastValue = value;
		int iteration = 0;
		while (lastValue != null) {

			if (iteration == 0) {
				lastValue = properties.put(key, lastValue);
			} else {
				lastValue = properties.put(key + "_" + iteration, lastValue);
			}
			iteration++;

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

	public void addPropertiesIfExist(Map<?, ?> value) {
		for (Map.Entry entry : value.entrySet()) {
			addProperty(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
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
		return clazz;
	}

	public void setStartTime(Long startTime) {
		this.startTime = startTime;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setClazz(String clazz) {
		if (clazz != null) {
			addProperty("SCLASS", clazz);
		}
		this.clazz = clazz;
	}

	public void setAdviceClass(Class adviceClass) {
		this.adviceClass = adviceClass.getSimpleName();
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

	public String getApplication() {
		return application;
	}

	public void setApplication(String application) {
		this.application = application;
	}

	public String getResource() {
		return resource;
	}

	public void setResource(String resource, ResourceType resourceType) {
		this.resource = resourceType.name() + "=" + resource;
	}

	public void setMode(Mode mode) {
		this.mode = mode;
	}

	public void stop() {
		mode = Mode.STOP;
	}

	public void setServer(String server) {
		this.server = server;
	}

	public String getServer() {
		return server;
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

	public void setTransparent() {
		transparent = true;
	}

	public void setTransparent(boolean b) {
		transparent = b;
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
