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

package com.jkoolcloud.remora.takes;

import static com.jkoolcloud.remora.takes.JSONUtils.quote;
import static java.text.MessageFormat.format;

import java.io.File;

import org.takes.Request;
import org.takes.Response;
import org.takes.Take;
import org.takes.rs.RsText;

import com.jkoolcloud.remora.Remora;
import com.jkoolcloud.remora.core.EntryDefinition;
import com.jkoolcloud.remora.core.output.AgentOutput;
import com.jkoolcloud.remora.core.output.ChronicleOutput;
import com.jkoolcloud.remora.core.output.OutputManager;
import com.jkoolcloud.remora.core.output.ScheduledQueueErrorReporter;

public class TkQueueStatistics implements Take {
	public static final String QUEUE_STATISTICS_RESPONSE_BODY = "'{'\n" + //
			"  \"memQErrorCount\" : {0},\n" + //
			"  \"memQCurrentSize\" : {1},\n" + //
			"  \"memQMaxSize\" : {2},\n" + //
			"  \"lastPersistQIndex\" : {3},\n" + //
			"  \"persistQErrorCount\" : {4},\n" + //
			"  \"lastException\": \"{5}\",\n" + //
			"  \"usableSpace\": {6}\n" + //

			"'}'";

	@Override
	public Response act(Request req) throws Exception {
		long usableSpace = 0;
		try {
			usableSpace = new File(System.getProperty(Remora.REMORA_PATH)).getUsableSpace();
		} catch (Throwable t) {

		}
		return new RsText(format(QUEUE_STATISTICS_RESPONSE_BODY, ScheduledQueueErrorReporter.intermediateQueueFailCount,
				quote(getCurrentIMMemSize()), ScheduledQueueErrorReporter.maxMemoryQueueSize,
				ScheduledQueueErrorReporter.lastIndexAppender, ScheduledQueueErrorReporter.chronicleQueueFailCount,
				ScheduledQueueErrorReporter.lastException, quote(usableSpace)));
	}

	private Object getCurrentIMMemSize() {
		AgentOutput<EntryDefinition> output = OutputManager.getOutput();
		if (output instanceof ChronicleOutput) {
			return ((ChronicleOutput) output).getImQueueSize();
		}
		return "N/A";

	}

}
