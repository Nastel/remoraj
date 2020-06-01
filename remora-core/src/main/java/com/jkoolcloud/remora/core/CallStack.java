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

package com.jkoolcloud.remora.core;

import java.util.Stack;

import org.tinylog.TaggedLogger;

public class CallStack<T> extends Stack<EntryDefinition> {
	private static final long serialVersionUID = 1273371157804943471L;

	private final TaggedLogger logger;

	private String application = null;
	private String server = null;
	private final String stackCorrelator;
	private final int limit;

	public CallStack(TaggedLogger logger, int limit) {
		this.logger = logger;
		this.limit = limit;
		stackCorrelator = JUGFactoryImpl.newUUID();
	}

	@Override
	public EntryDefinition push(EntryDefinition item) {
		// if (contains(item)) {
		// logger.info("Stack already contains ED");
		// return item;
		//
		// }
		if (size() >= limit) {
			if (logger != null) {
				logger.error("Stack limit reached: {}, {} : {}", (size() + 1), item.getAdviceClass(), item.getId());
			}
			return null;

		}

		if (logger != null) {
			logger.info("Stack push: {}, {} : {}", (size() + 1), item.getAdviceClass(), item.getId());
		}
		item.setApplication(application);
		item.setServer(server);
		item.setCorrelator(stackCorrelator);

		return super.push(item);
	}

	@Override
	public synchronized EntryDefinition pop() {
		EntryDefinition pop = super.pop();
		if (logger != null) {
			logger.info("Stack pop: {} : {} ", size(), pop.getId());
		}

		return pop;
	}

	public String getApplication() {
		return application;
	}

	public void setApplication(String application) {
		this.application = application;
		for (EntryDefinition entryDefinition : this) {
			entryDefinition.setApplication(application);
		}
	}

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
		for (EntryDefinition entryDefinition : this) {
			entryDefinition.setServer(server);
		}
	}
}
