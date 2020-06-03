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

import static com.jkoolcloud.remora.takes.TakesUtils.getValueForKey;

import java.lang.reflect.Field;

import org.takes.Request;
import org.takes.Response;
import org.takes.Take;
import org.takes.rq.RqPrint;
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
		String body = new RqPrint(req).printBody();
		String adviceName = getValueForKey("advice", body);
		String property = getValueForKey("property", body);
		String value = getValueForKey("value", body);

		return new RsText(applyChanges(adviceName, property, value));
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
