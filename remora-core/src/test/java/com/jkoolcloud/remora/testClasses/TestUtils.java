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

package com.jkoolcloud.remora.testClasses;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.FileUtils;

import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.queue.ExcerptAppender;
import net.openhft.chronicle.queue.ExcerptTailer;

public class TestUtils {

	public static class TempQueue implements Closeable {

		private final ExcerptTailer tailer;
		private final ExcerptAppender appender;
		private final ChronicleQueue queue;
		private final Path tempDirectory;

		public TempQueue() throws IOException {
			tempDirectory = Files.createTempDirectory(getClass().getName());
			queue = ChronicleQueue.single(tempDirectory.toFile().getAbsolutePath());
			appender = queue.acquireAppender();
			tailer = queue.createTailer();

		}

		public ChronicleQueue getQueue() {
			return queue;
		}

		public ExcerptTailer createTailer() {
			return tailer;
		}

		public ExcerptAppender acquireAppender() {
			return appender;
		}

		@Override
		public void close() throws IOException {
			queue.close();
			boolean success = false;
			while (!success) {
				try {
					Thread.sleep(900);
					FileUtils.deleteDirectory(tempDirectory.toFile());
					success = true;
				} catch (IOException | InterruptedException e) {
					success = false;
				}
			}

		}

	}
}
