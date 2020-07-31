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

import static java.text.MessageFormat.format;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import org.takes.Request;
import org.takes.Response;
import org.takes.facets.fork.RqRegex;
import org.takes.rs.RsText;

import com.jkoolcloud.remora.advices.StreamStats;
import com.jkoolcloud.remora.advices.StreamsManager;
import com.jkoolcloud.remora.core.EntryDefinition;

public class TKStreamDetails implements PluginTake {
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
		String io = ((RqRegex) req).matcher().group("io");
		HashMap<EntryDefinition, StreamStats> availableInputStreamsEntries = StreamsManager.INSTANCE
				.getAvailableInputStreamsEntries();
		HashMap<EntryDefinition, StreamStats> availableOutputStreamsEntries = StreamsManager.INSTANCE
				.getAvailableOutputStreamsEntries();

		String collect = Stream
				.concat(availableInputStreamsEntries.entrySet().stream(),
						availableOutputStreamsEntries.entrySet().stream())
				.filter(e -> String.valueOf(e.getKey().getClazz()).equals(io)).map(entry -> //
				format(STATS_RENTRY, //
						entry.getKey().getClazz(), //
						entry.getKey().getId(), //
						entry.getValue().count, //
						entry.getValue().accessTimestamp, //
						entry.getValue().starttime, //
						entry.getValue().accessCount, //
						getToStringValues(entry)))
				.collect(Collectors.joining(","));
		return new RsText(collect);
	}

	@NotNull
	private String getToStringValues(Map.Entry<EntryDefinition, StreamStats> entry) {
		return JSONUtils.addPadding(2,
				entry.getKey().getProperties().entrySet().stream().filter(e -> e.getKey().startsWith("toString"))
						.map(e -> JSONUtils.quote(e.getValue())).collect(Collectors.joining(",\n")));
	}

	@Override
	public String getEnpointPath() {
		return "/streams/(?<io>[^/]+)";
	}
}
