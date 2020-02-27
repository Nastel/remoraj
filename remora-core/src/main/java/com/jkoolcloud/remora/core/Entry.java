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

import com.jkoolcloud.remora.core.output.ChronicleOutput;
import com.jkoolcloud.remora.core.output.ScheduledQueueErrorReporter;

import net.openhft.chronicle.queue.ExcerptAppender;
import net.openhft.chronicle.wire.SelfDescribingMarshallable;

public class Entry extends SelfDescribingMarshallable implements Runnable {
	String id;
	EntryDefinition.Mode mode = EntryDefinition.Mode.RUNNING;
	String adviceClass;
	Long startTime;
	String name;
	String clazz;
	String stackTrace;
	String vmIdentification;
	String thread;

	@Override
	public void run() {
		ExcerptAppender appender = null;
		try {
			appender = ((ChronicleOutput.ChronicleAppenderThread) Thread.currentThread()).getAppender();
			// synchronized (properties) {
			appender.methodWriter(EntryDefinitionDescription.class).entry(this);

			// }
		} catch (Exception e) {
			if (appender != null) {
				ScheduledQueueErrorReporter.lastIndexAppender = appender.lastIndexAppended();
			}
			ScheduledQueueErrorReporter.chronicleQueueFailCount.incrementAndGet();
			ScheduledQueueErrorReporter.lastException = e;
		}
	}

	@Override
	public String toString() {
		return id;
	}
}
