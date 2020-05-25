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

import java.util.stream.Collectors;

import org.takes.Request;
import org.takes.Response;
import org.takes.rs.RsText;

import com.jkoolcloud.remora.advices.InputStreamManager;

public class TKStreams implements PluginTake {
	public static final String STATS_RENTRY = "'{'\n" //
			+ "  \"stream\" : \"{0}\",\n"//
			+ "  \"id\" : \"{1}\",\n"//
			+ "  \"bytes\" : \"{2}\",\n"//
			+ "  \"lastAccessed\" : {3},\n" //
			+ "  \"created\" : {4},\n"//
			+ "  \"itterations\": {5}\n"//
			+ "'}'";

	@Override
	public Response act(Request req) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("{\n");
		sb.append("\t\"activeInputStreams\":");

		sb.append("\t[\n");
		String inputStreamList = InputStreamManager.INSTANCE.getAvailableInputStreamsEntries().entrySet().stream()
				.map(entry -> //
				format(STATS_RENTRY, //
						entry.getKey().getClazz(), //
						entry.getKey().getId(), //
						entry.getValue().count, //
						entry.getValue().accessTimestamp, //
						entry.getValue().starttime, //
						entry.getValue().accessCount))
				.collect(Collectors.joining(","));
		sb.append(JSONUtils.addPadding(3, inputStreamList));
		sb.append("\t],\n");
		sb.append("\t\"activeOutputStreams\":");

		sb.append("\t[\n");
		String outputStreamList = InputStreamManager.INSTANCE.getAvailableOutputStreamsEntries().entrySet().stream()
				.map(entry -> //
				format(STATS_RENTRY, //
						entry.getKey().getClazz(), //
						entry.getKey().getId(), //
						entry.getValue().count, //
						entry.getValue().accessTimestamp, //
						entry.getValue().starttime, //
						entry.getValue().accessCount))
				.collect(Collectors.joining(","));
		sb.append(JSONUtils.addPadding(3, outputStreamList));
		sb.append("\t]\n");

		sb.append("}\n");
		return new RsText(sb.toString());
	}

	@Override
	public String getEnpointPath() {
		return "/streams";
	}
}
