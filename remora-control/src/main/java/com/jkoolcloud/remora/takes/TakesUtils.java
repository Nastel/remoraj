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

import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TakesUtils {
	protected static String getValueForKey(String key, String body) throws ParseException {
		Pattern pattern = Pattern.compile(String.format("\"%s\"\\s*:\\s*\"((?=[ -~])[^\"]+)\"", key));
		Matcher matcher = pattern.matcher(body);
		if (matcher.find()) {
			return matcher.group(1);
		} else {
			throw new ParseException(format("Cannot extract {} from \n {}", key, body), 0);
		}

	}

}
