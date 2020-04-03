/*
 *
 * Copyright (c) 2019-2020 NasTel Technologies, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of NasTel
 * Technologies, Inc. ("Confidential Information").  You shall not disclose
 * such Confidential Information and shall use it only in accordance with
 * the terms of the license agreement you entered into with NasTel
 * Technologies.
 *
 * NASTEL MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF
 * THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE, OR NON-INFRINGEMENT. NASTEL SHALL NOT BE LIABLE FOR ANY DAMAGES
 * SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
 * THIS SOFTWARE OR ITS DERIVATIVES.
 *
 * CopyrightVersion 1.0
 */

package com.jkoolcloud.remora.takes;

import static java.text.MessageFormat.format;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;

import org.takes.Request;
import org.takes.Response;
import org.takes.Take;
import org.takes.rs.RsText;

public class TkThreadDump implements Take {

	public static final String THREAD_DUMP_TEMPLATE = "'{'\n" + "  \"ThreadName\": \"{0}\",\n"
			+ "  \"ThreadState\": \"{1}\",\n" + "  \"StackTrace\": {2}\n" + "'}'";

	public static final String STACK_TRACE_TEMPLATE = "";

	@Override
	public Response act(Request req) throws Exception {

		StringBuilder dump = new StringBuilder();
		ThreadMXBean threadMxBean = ManagementFactory.getThreadMXBean();
		dump.append("[\n");
		ThreadInfo[] dumpAllThreads = threadMxBean.dumpAllThreads(true, true);
		for (int i1 = 0; i1 < dumpAllThreads.length; i1++) {
			ThreadInfo threadInfo = dumpAllThreads[i1];

			StringBuilder stackTraceArray = new StringBuilder();
			stackTraceArray.append("[\n");
			StackTraceElement[] stackTraceElements = threadInfo.getStackTrace();
			for (int i = 0; i < stackTraceElements.length; i++) {
				StackTraceElement stackTraceElement = stackTraceElements[i];
				stackTraceArray.append("\t\t");
				stackTraceArray.append("\"");
				stackTraceArray.append(escape(stackTraceElement.toString()));
				stackTraceArray.append("\"");

				if (i == stackTraceElements.length - 1) {
					stackTraceArray.append("\n");
				} else {
					stackTraceArray.append(",\n");
				}
			}
			stackTraceArray.append("\t\t]\n");

			String thread = format(THREAD_DUMP_TEMPLATE, escape(threadInfo.getThreadName()),
					threadInfo.getThreadState(), stackTraceArray.toString());
			dump.append(thread);

			if (i1 == dumpAllThreads.length - 1) {
				dump.append("\n");
			} else {
				dump.append(",\n");
			}

		}
		dump.append("\t\t]\n");

		return new RsText(dump.toString());
	}

	public static String escape(String jsString) {
		jsString = jsString.replace("\\", "\\\\");
		jsString = jsString.replace("\"", "\\\"");
		jsString = jsString.replace("\b", "\\b");
		jsString = jsString.replace("\f", "\\f");
		jsString = jsString.replace("\n", "\\n");
		jsString = jsString.replace("\r", "\\r");
		jsString = jsString.replace("\t", "\\t");
		jsString = jsString.replace("/", "\\/");
		return jsString;
	}
}
