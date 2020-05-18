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

package com.jkoolcloud.remora.advices;

import static com.jkoolcloud.remora.advices.BaseTransformers.doFinally;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.WeakHashMap;

import org.jetbrains.annotations.Nullable;
import org.tinylog.TaggedLogger;

import com.jkoolcloud.remora.core.EntryDefinition;

public enum InputStreamManager {

	INSTANCE;

	WeakHashMap<InputStream, EntryDefinition> availableInputStreams = new WeakHashMap<>();
	WeakHashMap<EntryDefinition, StreamStats> availableInputStreamsEntries = new WeakHashMap<>();

	WeakHashMap<OutputStream, EntryDefinition> availableOutputStreams = new WeakHashMap<>();
	WeakHashMap<EntryDefinition, StreamStats> availableOutputStreamsEntries = new WeakHashMap<>();

	public StreamStats get(InputStream thiz, TaggedLogger logger, Method method) {

		WeakHashMap<InputStream, EntryDefinition> availableInputStreams = this.availableInputStreams;
		WeakHashMap<EntryDefinition, StreamStats> availableInputStreamsEntries = this.availableInputStreamsEntries;

		EntryDefinition ed = null;
		ed = checkForEntryOrCreate(thiz, logger, method, availableInputStreams, availableInputStreamsEntries, ed);

		return availableInputStreamsEntries.get(ed);
	}

	public StreamStats get(OutputStream thiz, TaggedLogger logger, Method method) {

		WeakHashMap<OutputStream, EntryDefinition> availableOutputStreams = this.availableOutputStreams;
		WeakHashMap<EntryDefinition, StreamStats> availableOutputStreamsEntries = this.availableOutputStreamsEntries;

		EntryDefinition ed = null;
		ed = checkForEntryOrCreate(thiz, logger, method, availableOutputStreams, availableOutputStreamsEntries, ed);

		return availableInputStreamsEntries.get(ed);
	}

	public StreamStats close(InputStream thiz, TaggedLogger logger, Method method) {
		WeakHashMap<InputStream, EntryDefinition> availableStreams = availableInputStreams;
		WeakHashMap<EntryDefinition, StreamStats> availableStreamsEntries = availableInputStreamsEntries;

		return closeAndGenerateStats(thiz, logger, availableStreamsEntries, availableStreams);
	}

	public StreamStats close(OutputStream thiz, TaggedLogger logger, Method method) {
		WeakHashMap<OutputStream, EntryDefinition> availableStreams = availableOutputStreams;
		WeakHashMap<EntryDefinition, StreamStats> availableStreamsEntries = availableOutputStreamsEntries;

		return closeAndGenerateStats(thiz, logger, availableStreamsEntries, availableStreams);
	}

	@Nullable
	private static StreamStats closeAndGenerateStats(Object thiz, TaggedLogger logger,
			WeakHashMap<EntryDefinition, StreamStats> availableStreamsEntries,
			WeakHashMap<?, EntryDefinition> availableStreams) {
		boolean doFinally = true;
		try {
			// if (logging) {
			// logger.info("EntryDefinition not exist, entry might be filtered out as duplicate or ran on test");
			// }
			EntryDefinition ed = availableStreams.remove(thiz);
			if (ed == null) {
				if (logger != null) {
					logger.error("Stream closed but not tracked");
				}
				doFinally = false;
			} else {
				if (logger != null) {
					logger.info("Close invoked on stream " + ed.getId());
				}

				StreamStats streamStats = availableStreamsEntries.remove(ed);
				if (ed != null) {
					ed.addPropertyIfExist("count", streamStats.count);
					ed.addPropertyIfExist("lastAccessed", streamStats.accessTimestamp);
				} else if (logger != null) {
					logger.error("Stream closed but found no generated entry");
				}

				BaseTransformers.fillDefaultValuesAfter(ed, streamStats.starttime, null, logger);
			}
		} catch (Throwable t) {
			BaseTransformers.handleAdviceException(t, InputStreamManager.class.getSimpleName(), logger);
		} finally {
			if (doFinally) {
				doFinally(logger, thiz.getClass());
			}
		}
		return null;
	}

	private static <T> EntryDefinition checkForEntryOrCreate(T thiz, TaggedLogger logger, Method method,
			WeakHashMap<T, EntryDefinition> availableStreams,
			WeakHashMap<EntryDefinition, StreamStats> availableStreamsEntries, EntryDefinition ed) {
		if (!availableStreams.containsKey(thiz)) {

			ed = BaseTransformers.getEntryDefinition(ed, InputStreamReadAdvice.class, logger);
			StreamStats streamStats = new StreamStats();
			availableStreams.put(thiz, ed);
			availableStreamsEntries.put(ed, streamStats);
			if (logger != null) {
				logger.info("Crteatiung the new stream entry: " + ed.getId());
			}
			streamStats.starttime = BaseTransformers.fillDefaultValuesBefore(ed, BaseTransformers.stackThreadLocal,
					thiz, method, logger);

		}
		return ed;
	}
}
