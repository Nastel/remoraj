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

package com.jkoolcloud.remora.core.output;

import java.io.File;
import java.io.FileFilter;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Deque;
import java.util.concurrent.LinkedBlockingDeque;

import org.tinylog.Logger;
import org.tinylog.TaggedLogger;

import com.jkoolcloud.remora.Remora;
import com.jkoolcloud.remora.RemoraConfig;
import com.jkoolcloud.remora.core.EntryDefinition;

import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.queue.ExcerptAppender;
import net.openhft.chronicle.queue.RollCycles;
import net.openhft.chronicle.queue.impl.RollingChronicleQueue;
import net.openhft.chronicle.queue.impl.StoreFileListener;
import net.openhft.chronicle.wire.UnrecoverableTimeoutException;

public class ChronicleOutput implements OutputManager.AgentOutput<EntryDefinition> {

	TaggedLogger logger = Logger.tag("INIT");

	private ExcerptAppender appender;
	private ChronicleQueue queue;
	@RemoraConfig.Configurable
	String queuePath = System.getProperty(Remora.REMORA_PATH) + "/queue";

	@RemoraConfig.Configurable
	RollCycles rollCycle = RollCycles.valueOf("DAILY");

	@RemoraConfig.Configurable
	Long timeout = 5000L;

	@RemoraConfig.Configurable
	Integer keepQueueRolls = 2;

	Deque<File> unusedQueues;

	@Override
	public void init() {
		File queueDir = Paths.get(queuePath).toFile();
		unusedQueues = new LinkedBlockingDeque<>(keepQueueRolls);
		File[] cq4s = queueDir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.getName().endsWith("cq4");
			}
		});
		if (cq4s != null) {
            unusedQueues.addAll(Arrays.asList(cq4s));
        }

		logger.info("Writing to " + queueDir.getAbsolutePath());

		queue = ChronicleQueue.singleBuilder(queueDir.getPath()).rollCycle(rollCycle).timeoutMS(timeout)
				.storeFileListener(new StoreFileListener() {

					@Override
					public void onReleased(int cycle, File file) {
						while (!unusedQueues.offer(file)) {
							unusedQueues.removeFirst().delete();
						}

					}
				}).build();

		if (queue != null) {
			logger.info("Queue initialized " + this);
			if (queue instanceof RollingChronicleQueue) {
				((RollingChronicleQueue) queue).storeForCycle(((RollingChronicleQueue) queue).cycle(), 0, false);
			}
		} else {
			logger.error("Queue failed");
		}

		appender = queue.acquireAppender();
		if (appender != null) {
			logger.info("Appender initialized");
		} else {
			logger.error("Appender failed");
		}

	}

	@Override
	public void send(EntryDefinition entry) {
		try {
			appender.writeDocument(entry);
		} catch (UnrecoverableTimeoutException e) {
			logger.error(e);
		}

	}

	@Override
	public void shutdown() {
		logger.info("Shutting down chronicle queue:" + this);
		if (queue != null) {
			queue.close();
		}
	}

}
