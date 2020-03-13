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

package com.jkoolcloud.remora;

import java.lang.reflect.Field;
import java.nio.file.Paths;

import org.tinylog.Logger;
import org.tinylog.TaggedLogger;

import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.queue.ExcerptTailer;
import net.openhft.chronicle.queue.RollCycles;
import net.openhft.chronicle.threads.Pauser;

public enum RemoraControl {
	INSTANCE;

	ExcerptTailer tailer;
	String queuePath = System.getProperty(Remora.REMORA_PATH) + "/controlQueue";
	RollCycles rollCycle = RollCycles.valueOf("DAILY");
	Long timeout = 5000L;
	ChronicleQueue queue;
	Pauser pauser = Pauser.balanced();

	static TaggedLogger logger = Logger.tag("CONTROL");

	RemoraControl() {
	}

	public ExcerptTailer init() {

		Thread controlThread = new Thread(new Runnable() {
			@Override
			public void run() {
				for (;;) {
					if (!tailer.methodReader(new ControlImpl()).readOne()) {
						logger.debug("READ");

						pauser.pause();

					}
				}
			}
		});
		controlThread.setName("Remora control thread");
		if (queue == null) {
			queue = ChronicleQueue.singleBuilder(Paths.get(queuePath)).rollCycle(rollCycle).timeoutMS(timeout).build();
		} else {
			queue = queue;
		}

		tailer = queue.createTailer();
		controlThread.start();
		return tailer;
	}

	interface Control {
		ControlResponse control(ControlCommand command);
	}

	public static class ControlImpl implements Control {
		@Override
		public ControlResponse control(ControlCommand command) {
			try {
				Field field = command.adviceClass.getField(command.property);
				field.set(null, command.value);
				logger.debug("SETTING");
			} catch (NoSuchFieldException e) {
				logger.debug("No such field");
				return new ControlResponse(e);

			} catch (IllegalAccessException e) {
				logger.debug("IllegalAccessException");
				return new ControlResponse(e);
			}
			return new ControlResponse("Command ran successful");
		}
	}

	public static class ControlCommand {
		public Class adviceClass;
		public String property;
		public String value;

		public ControlCommand(Class adviceClass, String property, String value) {
			this.adviceClass = adviceClass;
			this.property = property;
			this.value = value;
		}
	}

	public static class ControlResponse {
		String message;
		Exception exception;

		public ControlResponse(Exception exception) {
			this.exception = exception;
		}

		public ControlResponse(String message) {
			this.message = message;
		}
	}
}
