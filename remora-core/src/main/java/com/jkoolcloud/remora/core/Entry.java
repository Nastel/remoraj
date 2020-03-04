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

import java.util.Objects;

import com.jkoolcloud.remora.core.output.ChronicleOutput;
import com.jkoolcloud.remora.core.output.ScheduledQueueErrorReporter;

import net.openhft.chronicle.queue.ExcerptAppender;
import net.openhft.chronicle.wire.SelfDescribingMarshallable;

public class Entry extends SelfDescribingMarshallable implements Runnable {
	protected static final int modelVersion = 1;
	protected String id;
	protected EntryDefinition.Mode mode = EntryDefinition.Mode.RUNNING;
	protected String adviceClass;
	protected Long startTime;
	protected String name;
	protected String clazz;
	protected String stackTrace;
	protected String vmIdentification;
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
				.write("mode").text(mode.name())//
				.write("adviceClass").text(adviceClass)//
				.write("startTime").writeLong(startTime)//
				.write("name").text(name)//
				.write("clazz").text(clazz)//
				.write("stackTrace").text(stackTrace)//
				.write("vmIdentification").text(vmIdentification)//
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
		return Objects.equals(id, entry.id) && mode == entry.mode && Objects.equals(adviceClass, entry.adviceClass)
				&& Objects.equals(startTime, entry.startTime) && Objects.equals(name, entry.name)
				&& Objects.equals(clazz, entry.clazz) && Objects.equals(stackTrace, entry.stackTrace)
				&& Objects.equals(vmIdentification, entry.vmIdentification) && Objects.equals(thread, entry.thread);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), id, mode, adviceClass, startTime, name, clazz, stackTrace,
				vmIdentification, thread);
	}

	@Override
	public String toString() {
		return id;
	}
}
