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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.jkoolcloud.remora.core.output.ChronicleOutput;
import com.jkoolcloud.remora.core.output.ScheduledQueueErrorReporter;

import net.openhft.chronicle.queue.ExcerptAppender;
import net.openhft.chronicle.wire.SelfDescribingMarshallable;

public class Exit extends SelfDescribingMarshallable implements Runnable {
	protected static final byte modelVersion = 1;
	protected String id;
	protected String name;
	protected EntryDefinition.Mode mode = EntryDefinition.Mode.STOP;
	protected String resource;
	protected EntryDefinition.ResourceType resourceType = EntryDefinition.ResourceType.GENERIC;
	protected String application;
	protected Map<String, String> properties = new HashMap<>(50);
	protected EntryDefinition.EventType eventType = EntryDefinition.EventType.CALL;
	protected String server;
	protected String exception;
	protected String correlator;
	protected String exceptionTrace;
	protected Long duration;

	@Override
	public void run() {
		ExcerptAppender appender = null;
		try {
			appender = ((ChronicleOutput.ChronicleAppenderThread) Thread.currentThread()).getAppender();
			// synchronized (properties) {
			// appender.methodWriter(EntryDefinitionDescription.class).exit(this);
			write(appender);
			// }
		} catch (Exception e) {
			if (appender != null) {
				ScheduledQueueErrorReporter.lastIndexAppender = appender.lastIndexAppended();
			}
			ScheduledQueueErrorReporter.chronicleQueueFailCount.incrementAndGet();
			ScheduledQueueErrorReporter.lastException = e;
		}
	}

	public void write(ExcerptAppender appender) {
		appender.writeDocument(w -> w.write("exit").marshallable(m -> m.write("id").text(id)//
				.write("v").fixedInt8(modelVersion)//
				.write("name").text(name)//
				.write("mode").text(mode.name())//
				.write("resource").text(resource)//
				.write("resourceType").text(resourceType.name())//
				.write("application").text(application)//
				.write("properties").marshallable(properties)//
				.write("eventType").text(eventType.name())//
				.write("exception").text(exception)//
				.write("correlator").text(correlator)//
				.write("exceptionTrace").text(exceptionTrace)//

		));
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		Exit exit = (Exit) o;
		return Objects.equals(modelVersion, modelVersion) && Objects.equals(id, exit.id)
				&& Objects.equals(name, exit.name) && mode == exit.mode && Objects.equals(resource, exit.resource)
				&& resourceType == exit.resourceType && Objects.equals(application, exit.application)
				&& Objects.equals(properties, exit.properties) && eventType == exit.eventType
				&& Objects.equals(server, exit.server) && Objects.equals(exception, exit.exception)
				&& Objects.equals(correlator, exit.correlator) && Objects.equals(exceptionTrace, exit.exceptionTrace)
				&& Objects.equals(duration, exit.duration);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), id, name, mode, resource, resourceType, application, properties,
				eventType, server, exception, correlator, exceptionTrace, duration);
	}

	@Override
	public String toString() {
		return id;
	}

}
