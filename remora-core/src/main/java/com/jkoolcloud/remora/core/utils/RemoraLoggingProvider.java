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

package com.jkoolcloud.remora.core.utils;

import org.tinylog.Level;
import org.tinylog.core.TinylogLoggingProvider;
import org.tinylog.format.MessageFormatter;
import org.tinylog.provider.ContextProvider;
import org.tinylog.provider.LoggingProvider;

import com.jkoolcloud.remora.AdviceRegistry;
import com.jkoolcloud.remora.advices.Logable;
import com.jkoolcloud.remora.advices.RemoraAdvice;

public class RemoraLoggingProvider implements LoggingProvider {

	private TinylogLoggingProvider realProvider = new TinylogLoggingProvider();

	public void reload() throws InterruptedException, ReflectiveOperationException {
		realProvider.shutdown();

		realProvider = new TinylogLoggingProvider();
	}

	@Override
	public ContextProvider getContextProvider() {
		return realProvider.getContextProvider();
	}

	@Override
	public Level getMinimumLevel() {
		return Level.TRACE;
	}

	@Override
	public Level getMinimumLevel(String tag) {
		return Level.TRACE;
	}

	@Override
	public boolean isEnabled(int depth, String tag, Level level) {
		return realProvider.isEnabled(depth + 1, tag, level);
	}

	@Override
	public void log(int depth, String tag, Level level, Throwable exception, MessageFormatter formatter, Object obj,
			Object... arguments) {
		try {
			RemoraAdvice adviceByName = AdviceRegistry.INSTANCE.getAdviceByName(tag);
			if (adviceByName instanceof Logable) {
				if (((Logable) adviceByName).getLogLevel().ordinal() > level.ordinal()) {
					return;
				}
			}
		} catch (ClassNotFoundException e) {
		}
		realProvider.log(depth + 1, tag, level, exception, formatter, obj, arguments);

	}

	@Override
	public void log(String loggerClassName, String tag, Level level, Throwable exception, MessageFormatter formatter,
			Object obj, Object... arguments) {
		realProvider.log(loggerClassName, tag, level, exception, formatter, obj, arguments);

	}

	@Override
	public void shutdown() throws InterruptedException {
		realProvider.shutdown();
	}

}
