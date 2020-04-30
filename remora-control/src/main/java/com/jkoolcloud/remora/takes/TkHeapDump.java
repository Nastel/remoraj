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

import java.io.File;
import java.lang.management.ManagementFactory;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.management.MBeanServer;

import org.takes.Request;
import org.takes.Response;
import org.takes.Take;
import org.takes.rs.RsText;

import com.jkoolcloud.remora.Remora;
import com.sun.management.HotSpotDiagnosticMXBean;

public class TkHeapDump implements Take {

	public static final String THREAD_DUMP_TEMPLATE = "'{'\n" + "  \"ThreadName\": \"{0}\",\n"
			+ "  \"ThreadState\": \"{1}\",\n" + "  \"StackTrace\": {2}\n" + "'}'";

	public static final String STACK_TRACE_TEMPLATE = "";

	@Override
	public Response act(Request req) throws Exception {

		MBeanServer server = ManagementFactory.getPlatformMBeanServer();
		HotSpotDiagnosticMXBean mxBean = ManagementFactory.newPlatformMXBeanProxy(server,
				"com.sun.management:type=HotSpotDiagnostic", HotSpotDiagnosticMXBean.class);
		String dumpFName = System.getProperty(Remora.REMORA_PATH, ".") + "/dumps/dump_"
				+ LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy_MM _d_HH_mm")) + ".hprof";
		File file = new File(dumpFName);
		File directory = file.getParentFile();
		if (!directory.exists() && !directory.isFile()) {
			directory.mkdirs();
		}
		if (file.exists()) {
			return new RsText("File already exist");
		}
		if (file.getUsableSpace() <= Runtime.getRuntime().totalMemory()) {
			mxBean.dumpHeap(dumpFName, true);
		} else {
			return new RsText("Out of disc space. Disk space required to complete operation.");
		}

		return new RsText("OK. Dump created: " + file.getAbsolutePath());
	}
}
