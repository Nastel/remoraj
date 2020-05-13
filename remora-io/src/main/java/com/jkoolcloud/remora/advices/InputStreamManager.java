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
import java.lang.reflect.Method;
import java.util.WeakHashMap;

import org.tinylog.TaggedLogger;

import com.jkoolcloud.remora.core.EntryDefinition;

public enum InputStreamManager {

	INSTANCE;

	WeakHashMap<InputStream, EntryDefinition> availableStreams = new WeakHashMap<>();
	WeakHashMap<EntryDefinition, StreamStats> availableStreamsEntries = new WeakHashMap<>();

	public StreamStats get(InputStream thiz, TaggedLogger logger, Method method) {
		EntryDefinition ed = null;

		if (!availableStreams.containsKey(thiz)) {

			ed = BaseTransformers.getEntryDefinition(ed, InputStreamReadAdvice.class, logger);
			StreamStats streamStats = new StreamStats();
			availableStreamsEntries.put(ed, streamStats);
			if (logger != null) {
				logger.info("Crteatiung the new stream entry: " + ed.getId());
			}
			streamStats.starttime = BaseTransformers.fillDefaultValuesBefore(ed, BaseTransformers.stackThreadLocal,
					thiz, method, logger);

		}

		return availableStreamsEntries.get(ed);
	}

	public StreamStats close(InputStream thiz, TaggedLogger logger, Method method) {
		boolean doFinally = true;
		try {
			// if (logging) {
			// logger.info("EntryDefinition not exist, entry might be filtered out as duplicate or ran on test");
			// }
			EntryDefinition ed = availableStreams.get(thiz);
			if (ed == null && logger != null) {
				logger.error("Stream closed but not tracked");
				doFinally = false;
			}
			if (logger != null) {
				logger.info("Close invoked on stream " + ed.getId());
			}
			StreamStats streamStats = availableStreamsEntries.get(ed);
			ed.addPropertyIfExist("count", streamStats.count);
			ed.addPropertyIfExist("lastAccessed", streamStats.accessTimestamp);

			BaseTransformers.fillDefaultValuesAfter(ed, streamStats.starttime, null, logger);
		} catch (Throwable t) {
			BaseTransformers.handleAdviceException(t, InputStreamCloseAdvice.ADVICE_NAME, logger);
		} finally {
			if (doFinally) {
				doFinally(logger, thiz.getClass());
			}
		}
		return null;
	}
}
