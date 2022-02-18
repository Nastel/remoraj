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

package com.jkoolcloud.remora.testClasses;

import static org.junit.Assert.fail;

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
			int successMaxTimes = 5;

			while (successMaxTimes > 0) {
				try {
					System.gc();
					Thread.sleep(900);
					FileUtils.deleteDirectory(tempDirectory.toFile());
					return;
				} catch (IOException | InterruptedException e) {
					successMaxTimes--;
				}
			}
			fail("Cannot delete old queue files");
		}
	}
}
