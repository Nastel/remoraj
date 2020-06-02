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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.takes.Request;
import org.takes.Response;
import org.takes.Take;
import org.takes.rs.RsText;

import com.jkoolcloud.remora.core.utils.ReflectionUtils;
import com.jkoolcloud.remora.filters.AdviceFilter;
import com.jkoolcloud.remora.filters.FilterManager;

public class TKFilters implements Take {

	public static final String FILTER_RESPONSE_TEMPLATE = "'{'\n" //
			+ "  \"filterName\" : \"{0}\",\n"//
			+ "  \"filterClass\" : \"{1}\",\n" //
			+ "  \"properties\" : '{'{2}'}\n"//
			+ "'}'";

	@Override
	public Response act(Request req) throws Exception {
		StringBuilder response = new StringBuilder();
		response.append("[");
		Map<String, AdviceFilter> filters = FilterManager.INSTANCE.getAll();
		response.append(filters.entrySet().stream().map(stringAdviceFilterEntry -> {

			List<String> properties = ReflectionUtils.getConfigurableFields(stringAdviceFilterEntry.getValue());
			Map<String, Object> stringObjectMap = ReflectionUtils.mapToCurrentValues(stringAdviceFilterEntry.getValue(),
					properties);
			String collect = stringObjectMap.entrySet().stream()
					.map(entry -> JSONUtils.quote(entry.getKey()) + " : " + JSONUtils.quote(entry.getValue()))
					.collect(Collectors.joining(",\n"));

			return format(FILTER_RESPONSE_TEMPLATE, stringAdviceFilterEntry.getKey(),
					stringAdviceFilterEntry.getValue().getClass(), JSONUtils.addPadding(4, collect));
		}).collect(Collectors.joining(",\n")));
		response.append("\n]");
		return new RsText(response.toString());

	}
}
