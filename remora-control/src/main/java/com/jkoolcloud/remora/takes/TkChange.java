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
			Field field = adviceByName.getClass().getField(property);
			if (!field.isAnnotationPresent(RemoraConfig.Configurable.class)) {
				throw new NoSuchFieldException();
			}
			Object appliedValue = RemoraConfig.getAppliedValue(field, value);
			field.set(null, appliedValue);
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
