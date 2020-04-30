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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jetbrains.annotations.NotNull;
import org.takes.Request;
import org.takes.Response;
import org.takes.Take;
import org.takes.rs.RsText;
import org.tinylog.TaggedLogger;

import com.jkoolcloud.remora.AdviceRegistry;
import com.jkoolcloud.remora.RemoraConfig;
import com.jkoolcloud.remora.advices.RemoraAdvice;
import com.jkoolcloud.remora.core.utils.ReflectionUtils;

public class TkChange implements Take {

	private TaggedLogger logger;

	public TkChange(TaggedLogger logger) {
		this.logger = logger;
	}

	@Override
	public Response act(Request req) throws Exception {
		String body = getBody(req.body());
		String adviceName = getValueForKey("advice", body);
		String property = getValueForKey("property", body);
		String value = getValueForKey("value", body);

		return new RsText(applyChanges(adviceName, property, value));
	}

	@NotNull
	protected static String getBody(InputStream t) throws IOException {
		StringBuilder bodySB = new StringBuilder();
		try (InputStreamReader reader = new InputStreamReader(t)) {
			char[] buffer = new char[256];
			int read;
			while ((read = reader.read(buffer)) != -1) {
				bodySB.append(buffer, 0, read);
			}
		}
		return bodySB.toString();
	}

	protected static String getValueForKey(String key, String body) throws ParseException {
		Pattern pattern = Pattern.compile(String.format("\"%s\"\\s*:\\s*\"((?=[ -~])[^\"]+)\"", key));
		Matcher matcher = pattern.matcher(body);
		if (matcher.find()) {
			return matcher.group(1);
		} else {
			throw new ParseException(format("Cannot extract {} from \n {}", key, body), 0);
		}

	}

	private String applyChanges(String adviceName, String property, String value) {
		try {
			logger.info("Ivoked remote request for \"{}\" property \"{}\" change. New value: {} ", adviceName, property,
					value);
			RemoraAdvice adviceByName = AdviceRegistry.INSTANCE.getAdviceByName(adviceName);
			Field field = ReflectionUtils.getFieldFromAllDeclaredFields(adviceByName.getClass(), property);
			if (!field.isAnnotationPresent(RemoraConfig.Configurable.class)) {
				throw new NoSuchFieldException();
			}
			Object appliedValue = RemoraConfig.getAppliedValue(field, value);
			field.set(adviceByName, appliedValue);
			return "OK";
		} catch (ClassNotFoundException e) {
			String m = "No such advice";
			logger.error("\t " + m + "\n {}", e);

			return m;
		} catch (IllegalAccessException e) {
			String m = "Cant change advices:" + property + " property: " + property;
			logger.error("\t " + m + "\n {}", e);
			return m;
		} catch (NoSuchFieldException e) {
			String m = "No such property";
			logger.error("\t " + m + "\n {}", e);
			return m;
		}
	}
}
