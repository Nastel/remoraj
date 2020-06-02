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

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;

public class JSONUtils {
	@NotNull
	static String addPadding(int padding, String template) {
		return Arrays.asList(template.split("\n")).stream().map(a -> a += "\n").map(a -> {
			for (int i = 0; i < padding; i++) {
				a += "\t";
			}
			return a;
		}).collect(Collectors.joining());
	}

	static String quote(Object value) {
		if (value instanceof Number) {
			// if (value instanceof )
			MessageFormat messageFormat = new MessageFormat("{0,number, #.##}", Locale.US);
			String format = messageFormat.format(new Object[] { value });
			return format;
		}
		if (value instanceof Boolean) {
			return String.valueOf(value);
		}
		if (value instanceof List) {
			return "[" + ((List) value).stream().map(e -> quote(e)).collect(Collectors.joining(",")) + "]";
		}
		return "\"" + String.valueOf(value) + "\"";
	}
}
