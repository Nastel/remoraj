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

import org.tinylog.TaggedLogger;

import com.jkoolcloud.remora.advices.BaseTransformers;

public class EmptyStack extends CallStack {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1089732400822680741L;
	private static final EntryDefinition.DummyEntryDefinition item = new EntryDefinition.DummyEntryDefinition();
	private final TaggedLogger logger;

	public EmptyStack(BaseTransformers.InterceptionContext ctx, int limit) {
		super(ctx, limit);
		logger = ctx.interceptorInstance.getLogger();
		logger.debug("EmptyStack created", ctx.interceptorInstance);
	}

	@Override
	public EntryDefinition push(EntryDefinition item) {
		logger.trace("Item pushed to empty stack", ctx.interceptorInstance);
		return item;
	}

	@Override
	public synchronized EntryDefinition pop() {
		return item;
	}

	@Override
	public synchronized EntryDefinition peek() {
		return item;
	}
}
