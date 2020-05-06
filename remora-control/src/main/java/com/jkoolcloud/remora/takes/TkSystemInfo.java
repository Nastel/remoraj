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

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.takes.Request;
import org.takes.Response;
import org.takes.Take;
import org.takes.rs.RsText;

public class TkSystemInfo implements Take {

	@Override
	public Response act(Request req) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("{\n");

		OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();

		String arch = operatingSystemMXBean.getArch();
		String name = operatingSystemMXBean.getName();
		int availableProcessors = operatingSystemMXBean.getAvailableProcessors();
		String version = operatingSystemMXBean.getVersion();

		sb.append("\t" + "\"OsName\" : " + quote(name) + ",\n");
		sb.append("\t" + "\"OsArch\" : " + quote(arch) + ",\n");
		sb.append("\t" + "\"AvailableProcessors\" : " + quote(availableProcessors) + ",\n");
		sb.append("\t" + "\"OsVersion\" : " + quote(version) + ",\n");

		String namesAndValues = Arrays.asList(operatingSystemMXBean.getClass().getDeclaredMethods()).stream()
				.filter(method -> method.getName().startsWith("get") && Modifier.isPublic(method.getModifiers()))
				.map(method -> {
					method.setAccessible(true);

					Object value;
					try {
						value = method.invoke(operatingSystemMXBean);
					} catch (Exception e) {
						value = e;
					}
					return ("\t" + quote(method.getName().substring(3)) + " : " + quote(String.valueOf(value)));

				}).collect(Collectors.joining(",\n"));

		sb.append(namesAndValues);
		sb.append("\n}\n");

		return new RsText(sb.toString());
	}

	private static String quote(Object value) {
		return "\"" + String.valueOf(value) + "\"";
	}
}
