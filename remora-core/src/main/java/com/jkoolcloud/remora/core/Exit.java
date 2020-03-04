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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.jkoolcloud.remora.core.output.ChronicleOutput;
import com.jkoolcloud.remora.core.output.ScheduledQueueErrorReporter;

import net.openhft.chronicle.queue.ExcerptAppender;
import net.openhft.chronicle.wire.SelfDescribingMarshallable;

public class Exit extends SelfDescribingMarshallable implements Runnable {
	protected static final int modelVersion = 1;
	protected String id;
	protected String name;
	protected EntryDefinition.Mode mode = EntryDefinition.Mode.STOP;
	protected String resource;
	protected EntryDefinition.ResourceType resourceType;
	protected String application;
	protected Map<String, String> properties = new HashMap<>();
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
		appender.writeDocument(w -> w.write("entry").marshallable(m -> m.write("id").text(id)//
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
		return Objects.equals(id, exit.id) && Objects.equals(name, exit.name) && mode == exit.mode
				&& Objects.equals(resource, exit.resource) && resourceType == exit.resourceType
				&& Objects.equals(application, exit.application) && Objects.equals(properties, exit.properties)
				&& eventType == exit.eventType && Objects.equals(server, exit.server)
				&& Objects.equals(exception, exit.exception) && Objects.equals(correlator, exit.correlator)
				&& Objects.equals(exceptionTrace, exit.exceptionTrace) && Objects.equals(duration, exit.duration);
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
