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

import java.lang.management.*;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.takes.Request;
import org.takes.Response;
import org.takes.Take;
import org.takes.rs.RsText;

public class TkGCInfo implements Take {

	public static final String MEM_GC_INFO_TEMPALTE = "'{'\n" //
			+ "  \"Total memory\": \"{0}\",\n"//
			+ "  \"Heap\": {1},\n"//
			+ "  \"NonHeap\": {2},\n"//
			+ "  \"GC\": {3}\n"//
			+ "  \"Details\": {4}\n"//

			+ "'}'";
	public static final String GC_INFO_TEMPALTE = "'{'\n" + "  \"Name\": \"{0}\",\n"//
			+ "  \"Collections\": \"{1}\",\n"//
			+ "  \"LastCollectionTime\": {2},\n"//
			+ "  \"PoolNames\": {3}\n"//
			+ "'}'";

	public static final String MEM_TEMPLATE = "'{'\n"//
			+ "  \"Name\": \"{0}\",\n" //
			+ "  \"Type\": \"{1}\",\n"//
			+ "  \"Used\": {2},\n" //
			+ "  \"Collections\": {3}\n" //
			+ "'}'";

	@Override
	public Response act(Request req) throws Exception {
		StringBuilder gcb = new StringBuilder();
		StringBuilder sb = new StringBuilder();
		StringBuilder mem = new StringBuilder();

		for (GarbageCollectorMXBean gc : ManagementFactory.getGarbageCollectorMXBeans()) {
			String name = gc.getName() + " " + gc.getClass().getName();
			long collectionCount = gc.getCollectionCount();
			long collectionTime = gc.getCollectionTime();
			String[] memoryPoolNames = gc.getMemoryPoolNames();
			gcb.append(format(addPadding(2, GC_INFO_TEMPALTE), name, collectionCount, collectionTime,
					Arrays.toString(memoryPoolNames)));
			gcb.append(",");
		}
		MemoryUsage heapMemoryUsage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
		MemoryUsage nonHeapUsage = ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage();

		for (MemoryPoolMXBean memoryPoolMXBean : ManagementFactory.getMemoryPoolMXBeans()) {
			String name = memoryPoolMXBean.getName();
			MemoryUsage collectionUsage = memoryPoolMXBean.getCollectionUsage();
			MemoryUsage peakUsage = memoryPoolMXBean.getPeakUsage();
			MemoryType type = memoryPoolMXBean.getType();
			mem.append(format(addPadding(2, MEM_TEMPLATE), name, type.toString(), usageToJSON(collectionUsage, 4),
					usageToJSON(peakUsage, 4)));
		}

		sb.append(format(MEM_GC_INFO_TEMPALTE, "", usageToJSON(heapMemoryUsage, 2), usageToJSON(nonHeapUsage, 2),
				gcb.toString(), mem.toString()));

		return new RsText(sb.toString());
	}

	private String usageToJSON(MemoryUsage collectionUsage, int padding) {
		if (collectionUsage == null) {
			return "N/A";
		}
		String USAGE_TEMPLATE = "'{'\n"//
				+ "  \"Init\": \"{0}\",\n" //
				+ "  \"Used\": \"{1}\",\n"//
				+ "  \"Max\": \"{2}\",\n" //
				+ "  \"Commited\": \"{3}\"" //
				+ "'}'";

		long init = collectionUsage.getInit();
		long used = collectionUsage.getUsed();
		long max = collectionUsage.getMax();
		long committed = collectionUsage.getCommitted();
		String paddedPattern = addPadding(padding, USAGE_TEMPLATE);

		return format(paddedPattern, init, used, max, committed);
	}

	@NotNull
	private String addPadding(int padding, String template) {
		return Arrays.asList(template.split("\n")).stream().map(a -> a += "\n").map(a -> {
			for (int i = 0; i < padding; i++) {
				a += "\t";
			}
			return a;
		}).collect(Collectors.joining());
	}

}
