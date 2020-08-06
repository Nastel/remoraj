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

import java.util.WeakHashMap;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.takes.Request;
import org.takes.Response;
import org.takes.rs.RsText;

import com.jkoolcloud.remora.advices.StreamStats;
import com.jkoolcloud.remora.advices.StreamsManager;
import com.jkoolcloud.remora.core.EntryDefinition;

public class TKStreams implements PluginTake {
	public static final String STATS_RENTRY = "'{'\n" //
			+ "  \"stream\" : \"{0}\",\n"//
			+ "  \"id\" : \"{1}\",\n"//
			+ "  \"bytes\" : {2,number,#},\n"//
			+ "  \"lastAccessed\" : {3,number,#},\n" //
			+ "  \"lastAccessedReadable\" : \"{3, date} {3,time}\",\n" //
			+ "  \"created\" : {4,number,#},\n"//
			+ "  \"createdReadable\" : \"{4,date} {4,time}\",\n"//
			+ "  \"invocations\": {5,number,#},\n"//
			+ "  \"instances\": [{6}]\n"//
			+ "'}'";

	@Override
	public Response act(Request req) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("{\n");
		sb.append("\t\"totalTrackedInputStreams\": ");
		sb.append(StreamsManager.INSTANCE.totalTrackedInputStreams.get());
		sb.append(",\n");
		sb.append("\t\"totalTrackedOutputStreams\": ");
		sb.append(StreamsManager.INSTANCE.totalTrackedOutputStreams.get());
		sb.append(",\n");
		sb.append("\t\"totalActiveInputSteams\": ");
		sb.append(StreamsManager.INSTANCE.getAvailableInputStreams().size());
		sb.append(",\n");
		sb.append("\t\"totalUniqueInputStreams\": ");
		sb.append(StreamsManager.INSTANCE.getAvailableInputStreamsEntries().size());
		sb.append(",\n");
		sb.append("\t\"totalActiveOutputSteams\": ");
		sb.append(StreamsManager.INSTANCE.getAvailableOutputStreams().size());
		sb.append(",\n");
		sb.append("\t\"totalUniqueOutputStreams\": ");
		sb.append(StreamsManager.INSTANCE.getAvailableOutputStreamsEntries().size());
		sb.append(",\n");
		sb.append("\t\"activeStreamsBytesRead\": ");
		sb.append(StreamsManager.INSTANCE.getAvailableInputStreamsEntries().values().stream()

				.mapToLong(value -> value.count.get()).sum());
		sb.append(",\n");
		sb.append("\t\"activeStreamsBytesWrite\": ");
		sb.append(StreamsManager.INSTANCE.getAvailableOutputStreamsEntries().values().stream()
				.mapToLong(value -> value.count.get()).sum());
		sb.append(",\n");

		sb.append("\t\"activeInputStreams\":");

		sb.append("\t{\n");
		String inputStreamList = getStreamList(StreamsManager.INSTANCE.getAvailableInputStreamsEntries());
		sb.append(JSONUtils.addPadding(0, inputStreamList));
		sb.append("\t},\n");
		sb.append("\t\"activeOutputStreams\":");

		sb.append("\t{\n");
		String outputStreamList = getStreamList(StreamsManager.INSTANCE.getAvailableOutputStreamsEntries());
		sb.append(JSONUtils.addPadding(0, outputStreamList));
		sb.append("\t}\n");

		sb.append("}\n");
		return new RsText(sb.toString());
	}

	@NotNull
	private String getStreamList(WeakHashMap<EntryDefinition, StreamStats> availableInputStreamsEntries) {

		return availableInputStreamsEntries.entrySet().stream()
				.collect(Collectors
						.groupingBy(entry -> entry.getKey().getClazz() == null ? "null" : entry.getKey().getClazz()))
				.entrySet().stream()
				.map(entry -> "\t\t" + JSONUtils.quote(entry.getKey()) + " : " + entry.getValue().size())
				.collect(Collectors.joining(",\n"));

	}

	@Override
	public String getEnpointPath() {
		return "/streams";
	}
}
