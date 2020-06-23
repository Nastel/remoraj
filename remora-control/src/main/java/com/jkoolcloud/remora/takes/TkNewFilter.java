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

import java.lang.reflect.Field;
import java.text.ParseException;
import java.util.Arrays;

import org.takes.Request;
import org.takes.Response;
import org.takes.Take;
import org.takes.rq.RqPrint;
import org.takes.rs.RsText;
import org.tinylog.TaggedLogger;

import com.jkoolcloud.remora.RemoraConfig;
import com.jkoolcloud.remora.filters.FilterManager;
import com.jkoolcloud.remora.filters.StatisticEnabledFilter;

public class TkNewFilter implements Take {

	private TaggedLogger logger;

	public TkNewFilter(TaggedLogger logger) {
		this.logger = logger;
	}

	@Override
	public Response act(Request req) throws Exception {
		String body = new RqPrint(req).printBody();
		String filterClass = TakesUtils.getValueForKey("class", body);
		String filterName = TakesUtils.getValueForKey("name", body);

		StatisticEnabledFilter filterInstance = null;
		try {
			filterInstance = (StatisticEnabledFilter) Class.forName(filterClass).newInstance();
			for (Field field : Arrays.asList(filterInstance.getClass().getDeclaredFields())) {
				if (field.isAnnotationPresent(RemoraConfig.Configurable.class)) {
					try {
						String valueForKey = TakesUtils.getValueForKey(field.getName(), body);
						Object appliedValue = RemoraConfig.getAppliedValue(field, valueForKey);
						if (valueForKey != null) {
							field.set(filterInstance, appliedValue);
						}
					} catch (ParseException e) {
					} catch (IllegalAccessException ex) {
						logger.error(ex);
						return new RsText("Filter failed to configure field: " + field.getName());
					}
				}
			}

		} catch (InstantiationException e) {
			logger.error(e);
		} catch (IllegalAccessException e) {
			logger.error(e);
		} catch (ClassNotFoundException e) {
			logger.error(e);
		} catch (SecurityException e) {
			logger.error(e);
		}
		if (filterInstance != null) {
			FilterManager.INSTANCE.add(filterName, filterInstance);
			return new RsText("Filter created");
		} else {
			return new RsText("Filter not created");
		}
	}
}
