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

import java.util.Objects;

import com.jkoolcloud.remora.core.output.ChronicleOutput;
import com.jkoolcloud.remora.core.output.ScheduledQueueErrorReporter;

import net.openhft.chronicle.queue.ExcerptAppender;
import net.openhft.chronicle.wire.SelfDescribingMarshallable;

public class Entry extends SelfDescribingMarshallable implements Runnable {
	protected static final byte modelVersion = 1;
	protected String id;
	protected EntryDefinition.Mode mode = EntryDefinition.Mode.RUNNING;
	protected String adviceClass;
	protected long startTime;
	protected String name;
	protected String clazz;
	protected String stackTrace;
	protected String vmId;
	protected String thread;

	@Override
	public void run() {
		ExcerptAppender appender = null;
		try {
			appender = ((ChronicleOutput.ChronicleAppenderThread) Thread.currentThread()).getAppender();
			// synchronized (properties) {
			// appender.methodWriter(EntryDefinitionDescription.class).entry(this);
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
				.write("v").fixedInt8(modelVersion).write("mode").text(mode.name())//
				.write("adviceClass").text(adviceClass)//
				.write("startTime").fixedInt64(startTime)//
				.write("name").text(name)//
				.write("clazz").text(clazz)//
				.write("stackTrace").text(stackTrace)//
				.write("vmId").text(vmId)//
				.write("thread").text(thread)//
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

		Entry entry = (Entry) o;
		return Objects.equals(modelVersion, modelVersion) && Objects.equals(id, entry.id) && mode == entry.mode
				&& Objects.equals(adviceClass, entry.adviceClass) && Objects.equals(startTime, entry.startTime)
				&& Objects.equals(name, entry.name) && Objects.equals(clazz, entry.clazz)
				&& Objects.equals(stackTrace, entry.stackTrace) && Objects.equals(vmId, entry.vmId)
				&& Objects.equals(thread, entry.thread);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), id, mode, adviceClass, startTime, name, clazz, stackTrace, vmId, thread);
	}

	@Override
	public String toString() {
		return id;
	}
}
