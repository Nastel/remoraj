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

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryType;
import java.lang.management.MemoryUsage;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.takes.Request;
import org.takes.Response;
import org.takes.Take;
import org.takes.rs.RsText;

public class TkGCInfo implements Take {

	public static final String MEM_GC_INFO_TEMPALTE = "'{'\n" //
			+ "  \"Heap\": {0},\n"//
			+ "  \"NonHeap\": {1},\n"//
			+ "  \"GC\": [{2}],\n"//
			+ "  \"Details\": [{3}]\n"//

			+ "'}'";
	public static final String GC_INFO_TEMPALTE = "'{'\n" + "  \"Name\": \"{0}\",\n"//
			+ "  \"Collections\": {1,number,#},\n"//
			+ "  \"LastCollectionTime\": {2,number,#},\n"//
			+ "  \"PoolNames\": [{3}]\n"//
			+ "'}'";

	public static final String MEM_TEMPLATE = "'{'\n"//
			+ "  \"Name\": \"{0}\",\n" //
			+ "  \"Type\": \"{1}\",\n"//
			+ "  \"Used\": {2},\n" //
			+ "  \"Collections\": {3}\n" //
			+ "'}'";

	public static final String USAGE_TEMPLATE = "'{'\n"//
			+ "  \"Init\": {0,number,#},\n" //
			+ "  \"Used\": {1,number,#},\n"//
			+ "  \"Max\": {2,number,#},\n" //
			+ "  \"Commited\": {3,number,#}" //
			+ "'}'";

	@Override
	public Response act(Request req) throws Exception {
		StringBuilder sb = new StringBuilder();

		String gcb = ManagementFactory.getGarbageCollectorMXBeans().stream().map(gc -> {
			String name = gc.getName();
			long collectionCount = gc.getCollectionCount();
			long collectionTime = gc.getCollectionTime();
			String[] memoryPoolNames = gc.getMemoryPoolNames();
			return format(JSONUtils.addPadding(2, GC_INFO_TEMPALTE), name, collectionCount, collectionTime,
					returnArray(memoryPoolNames));
		}).collect(Collectors.joining(","));

		MemoryUsage heapMemoryUsage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
		MemoryUsage nonHeapUsage = ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage();

		String mem = ManagementFactory.getMemoryPoolMXBeans().stream().map(memoryPoolMXBean -> {
			String name = memoryPoolMXBean.getName();
			MemoryUsage collectionUsage = memoryPoolMXBean.getCollectionUsage();
			MemoryUsage peakUsage = memoryPoolMXBean.getPeakUsage();
			MemoryType type = memoryPoolMXBean.getType();
			return format(JSONUtils.addPadding(2, MEM_TEMPLATE), name, type.toString(), usageToJSON(collectionUsage, 4),
					usageToJSON(peakUsage, 4));
		}).collect(Collectors.joining(","));

		sb.append(format(MEM_GC_INFO_TEMPALTE, usageToJSON(heapMemoryUsage, 2), usageToJSON(nonHeapUsage, 2), gcb,
				mem.toString()));

		return new RsText(sb.toString());
	}

	private String returnArray(String[] memoryPoolNames) {
		return Arrays.asList(memoryPoolNames).stream().map(a -> "\"" + a + "\"").collect(Collectors.joining(","));
	}

	private String usageToJSON(MemoryUsage collectionUsage, int padding) {
		if (collectionUsage == null) {
			return "\"N/A\"";
		}

		long init = collectionUsage.getInit();
		long used = collectionUsage.getUsed();
		long max = collectionUsage.getMax();
		long committed = collectionUsage.getCommitted();
		String paddedPattern = JSONUtils.addPadding(padding, USAGE_TEMPLATE);

		return format(paddedPattern, init, used, max, committed);
	}

}
