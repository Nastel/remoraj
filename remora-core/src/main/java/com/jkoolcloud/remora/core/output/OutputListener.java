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

public interface OutputListener {
	/**
	 * Will be triggered on OutputManager before handling it to real AgentOutputs
	 */
	void beforeSend();

	/**
	 * Will be triggered on initialising the output
	 */
	void onInitialize();

	/**
	 * Wil be triggered after AgentOutputs initializes
	 *
	 * @param e
	 *            Exception would be filled if one occurs
	 */
	void onInitialized(Exception e);

	/**
	 * Will be triggered at AgentOutputs send
	 */

	void onSend();

	/**
	 * Will be triggered on AgentsOutput shutdown
	 */

	void onShutdown();

	/**
	 * Will be triggered when sending finished
	 *
	 * @param e
	 *            Exception would be filled if one occurs on sending
	 */
	void onSent(Exception e);

	class OutputLogger implements OutputListener {

		private static final TaggedLogger logger = Logger.tag("INFO");

		@Override
		public void beforeSend() {

		}

		@Override
		public void onInitialize() {

		}

		@Override
		public void onInitialized(Exception e) {

		}

		@Override
		public void onSend() {

		}

		@Override
		public void onShutdown() {
			logger.info("Shutting down: ");
		}

		@Override
		public void onSent(Exception e) {
			if (e == null) {
				logger.info("Message sent");
			} else {
				logger.error("Cannot send the message");
			}
		}
	}
}
