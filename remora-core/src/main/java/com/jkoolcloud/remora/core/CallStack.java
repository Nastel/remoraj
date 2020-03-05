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

import java.util.Stack;

import org.tinylog.TaggedLogger;

public class CallStack<T> extends Stack<EntryDefinition> {
	private static final long serialVersionUID = 1273371157804943471L;

	private final TaggedLogger logger;

	private String application;
	private String server;
	private String stackCorrelator;

	public CallStack(TaggedLogger logger) {
		this.logger = logger;
		stackCorrelator = JUGFactoryImpl.newUUID();
	}

	@Override
	public EntryDefinition push(EntryDefinition item) {
		// if (contains(item)) {
		// logger.info("Stack already contains ED");
		// return item;
		//
		// }

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
