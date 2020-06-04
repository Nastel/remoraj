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
import java.util.HashMap;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.jetbrains.annotations.Nullable;
import org.tinylog.TaggedLogger;

import com.jkoolcloud.remora.core.EntryDefinition;

public enum InputStreamManager {

	INSTANCE;

	public final AtomicLong totalTrackedInputStreams = new AtomicLong();
	public final AtomicLong totalTrackedOutputStreams = new AtomicLong();

	WeakHashMap<InputStream, EntryDefinition> availableInputStreams = new CountingWeakHashMap(totalTrackedInputStreams);
	HashMap<EntryDefinition, StreamStats> availableInputStreamsEntries = new HashMap<>(500);

	WeakHashMap<OutputStream, EntryDefinition> availableOutputStreams = new CountingWeakHashMap(
			totalTrackedOutputStreams);
	HashMap<EntryDefinition, StreamStats> availableOutputStreamsEntries = new HashMap<>(500);

	public StreamStats get(InputStream thiz, TaggedLogger logger, Method method) {

		WeakHashMap<InputStream, EntryDefinition> availableInputStreams = this.availableInputStreams;
		HashMap<EntryDefinition, StreamStats> availableInputStreamsEntries = this.availableInputStreamsEntries;

		EntryDefinition ed = null;
		ed = checkForEntryOrCreate(thiz, logger, method, availableInputStreams, availableInputStreamsEntries, ed);

		return availableInputStreamsEntries.get(ed);
	}

	public StreamStats get(OutputStream thiz, TaggedLogger logger, Method method) {

		WeakHashMap<OutputStream, EntryDefinition> availableOutputStreams = this.availableOutputStreams;
		HashMap<EntryDefinition, StreamStats> availableOutputStreamsEntries = this.availableOutputStreamsEntries;

		EntryDefinition ed = null;
		ed = checkForEntryOrCreate(thiz, logger, method, availableOutputStreams, availableOutputStreamsEntries, ed);

		return availableInputStreamsEntries.get(ed);
	}

	public StreamStats close(InputStream thiz, TaggedLogger logger, Method method) {
		WeakHashMap<InputStream, EntryDefinition> availableStreams = availableInputStreams;
		HashMap<EntryDefinition, StreamStats> availableStreamsEntries = availableInputStreamsEntries;

		return closeAndGenerateStats(thiz, logger, availableStreamsEntries, availableStreams);
	}

	public StreamStats close(OutputStream thiz, TaggedLogger logger, Method method) {
		WeakHashMap<OutputStream, EntryDefinition> availableStreams = availableOutputStreams;
		HashMap<EntryDefinition, StreamStats> availableStreamsEntries = availableOutputStreamsEntries;

		return closeAndGenerateStats(thiz, logger, availableStreamsEntries, availableStreams);
	}

	@Nullable
	private static StreamStats closeAndGenerateStats(Object thiz, TaggedLogger logger,
			HashMap<EntryDefinition, StreamStats> availableStreamsEntries,
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
				if (ed != null) {
					StreamStats streamStats = streamStats = availableStreamsEntries.get(ed);

					BaseTransformers.fillDefaultValuesAfter(ed, streamStats.starttime, null, logger);
					if (ed.isFinished()) {
						availableStreamsEntries.remove(ed);
						ed.addPropertyIfExist("bytesCount", streamStats.count);
						ed.addPropertyIfExist("lastAccessed", streamStats.accessTimestamp);
						ed.addPropertyIfExist("accessCount", streamStats.accessCount);
					}
				} else if (logger != null) {
					logger.error("Stream closed but found no generated entry");
				}

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
			HashMap<EntryDefinition, StreamStats> availableStreamsEntries, EntryDefinition ed) {
		if (!availableStreams.containsKey(thiz)) {

			ed = BaseTransformers.getEntryDefinition(ed, InputStreamReadAdvice.class, logger);
			availableStreams.put(thiz, ed);

			if (!availableStreamsEntries.containsKey(ed)) {
				StreamStats streamStats = new StreamStats();
				if (logger != null) {
					logger.info("Creating the new stream stats: " + ed.getId());
				}
				streamStats.starttime = BaseTransformers.fillDefaultValuesBefore(ed, BaseTransformers.stackThreadLocal,
						thiz, method, logger);
				ed.addProperty("toString", String.valueOf(thiz));
				availableStreamsEntries.put(ed, streamStats);
			}
			if (logger != null) {
				logger.info("Created the new stream entry: " + ed.getId());
			}

		} else {
			ed = availableStreams.get(thiz);
		}

		return ed;
	}

	public HashMap<EntryDefinition, StreamStats> getAvailableInputStreamsEntries() {
		return availableInputStreamsEntries;
	}

	public HashMap<EntryDefinition, StreamStats> getAvailableOutputStreamsEntries() {
		return availableOutputStreamsEntries;
	}

	// For test only
	public void setAvailableInputStreamsEntries(HashMap<EntryDefinition, StreamStats> availableInputStreamsEntries) {
		this.availableInputStreamsEntries = availableInputStreamsEntries;
	}

	public void setAvailableOutputStreamsEntries(HashMap<EntryDefinition, StreamStats> availableOutputStreamsEntries) {
		this.availableOutputStreamsEntries = availableOutputStreamsEntries;
	}

	public WeakHashMap<InputStream, EntryDefinition> getAvailableInputStreams() {
		return availableInputStreams;
	}

	public WeakHashMap<OutputStream, EntryDefinition> getAvailableOutputStreams() {
		return availableOutputStreams;
	}

	private static class CountingWeakHashMap<K, V> extends WeakHashMap {
		final AtomicLong count;

		public CountingWeakHashMap(AtomicLong countVar) {
			super(500);
			count = countVar;
		}

		@Override
        public V put(Object key, Object value) {
			count.incrementAndGet();
			return (V) super.put(key, value);
		}

	}
}
