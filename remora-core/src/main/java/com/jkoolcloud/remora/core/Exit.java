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

import com.jkoolcloud.remora.core.output.ChronicleOutput;
import com.jkoolcloud.remora.core.output.ScheduledQueueErrorReporter;

import net.openhft.chronicle.queue.ExcerptAppender;
import net.openhft.chronicle.wire.SelfDescribingMarshallable;

public class Exit extends SelfDescribingMarshallable implements Runnable {
	String id;
	String name;
	EntryDefinition.Mode mode = EntryDefinition.Mode.STOP;
	String resource;
	EntryDefinition.ResourceType resourceType;
	String application;
	Map<String, String> properties = new HashMap<>();
	EntryDefinition.EventType eventType = EntryDefinition.EventType.CALL;
	String server;
	// Workaround for serialization, static fields not serialised
	String exception;
	String correlator;
	String exceptionTrace;
	Long duration;

	@Override
	public void run() {
		ExcerptAppender appender = null;
		try {
			appender = ((ChronicleOutput.ChronicleAppenderThread) Thread.currentThread()).getAppender();
			// synchronized (properties) {
			appender.methodWriter(EntryDefinitionDescription.class).exit(this);

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
