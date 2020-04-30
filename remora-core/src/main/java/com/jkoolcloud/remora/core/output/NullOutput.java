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

package com.jkoolcloud.remora.core.output;

import org.tinylog.Logger;
import org.tinylog.TaggedLogger;

import com.jkoolcloud.remora.core.EntryDefinition;

public class NullOutput implements OutputManager.AgentOutput<EntryDefinition> {

	TaggedLogger logger = Logger.tag("INIT");

	@Override
	public void init() {
		logger.info("NULL output initialised");
	}

	@Override
	public void send(EntryDefinition entry) {
	}

	@Override
	public void shutdown() {

	}
}
