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

import java.util.ArrayList;
import java.util.List;

import org.tinylog.Logger;
import org.tinylog.TaggedLogger;

import com.jkoolcloud.remora.RemoraConfig;
import com.jkoolcloud.remora.core.EntryDefinition;

public enum OutputManager {
	INSTANCE;

	// If anyone wonders why it's not static
	// https://stackoverflow.com/questions/49141972/nullpointerexception-in-enum-logger
	private final TaggedLogger logger = Logger.tag("INFO");

	private static boolean shutdown = false;

	private static AgentOutput<EntryDefinition> output;
	private static List<OutputListener> outputListeners;

	OutputManager() {
		install();
	}

	@SuppressWarnings("unchecked")
	public void install() {
		logger.info("Starting OutputManager");
		outputListeners = new ArrayList<>(10);
		String outputClass = System.getProperty("remora.output");
		if (outputClass != null) {
			try {
				Class<?> outClass = Class.forName(outputClass);
				output = (AgentOutput<EntryDefinition>) outClass.newInstance();
			} catch (Exception e) {
				outputListeners.forEach(l -> l.onInitialized(e));
			}
			outputListeners.forEach(l -> l.onInitialized(null));
		} else {
			output = new ChronicleOutput();
		}
		try {
			RemoraConfig.configure(output);
			synchronized (output) {
				try {
					outputListeners.forEach(listener -> listener.onInitialize());
					output.init();
				} catch (AgentOutput.OutputException e) {
					outputListeners.forEach(l -> l.onInitialized(e));
				}
				outputListeners.forEach(l -> l.onInitialized(null));

			}

			Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				synchronized (output) {
					shutdown = true;
					outputListeners.forEach(l -> l.onShutdown());
					output.shutdown();
				}
			}));
		} catch (Exception e) {
			logger.error("Failed Starting OutputManager");
		}

	}

	public static AgentOutput<EntryDefinition> getOutput() {
		return OutputManager.output;
	}

	public static void send(EntryDefinition entryDefinition) {
		if (output != null) {
			outputListeners.forEach(l -> l.onSend());
			output.send(entryDefinition);
		} else {
			outputListeners.forEach(l -> l.onSent(new AgentOutput.OutputException("No output initialised")));
		}
	}

}
