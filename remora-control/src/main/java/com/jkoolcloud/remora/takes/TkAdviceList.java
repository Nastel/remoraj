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

import static com.jkoolcloud.remora.takes.JSONUtils.quote;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.takes.Request;
import org.takes.Response;
import org.takes.Take;
import org.takes.rs.RsText;

import com.jkoolcloud.remora.AdviceRegistry;
import com.jkoolcloud.remora.Remora;
import com.jkoolcloud.remora.advices.RemoraAdvice;
import com.jkoolcloud.remora.core.utils.ReflectionUtils;

public class TkAdviceList implements Take {

	@Override
	public Response act(Request req) throws Exception {
		StringBuilder response = new StringBuilder();
		response.append("{\n");
		response.append("\"version\" : \"" + Remora.getVersion() + "\",\n");
		response.append("\"vmid\" : \"" + System.getProperty(Remora.REMORA_VM_IDENTIFICATION) + "\",\n"); // default
																											// update
																											// docs
		response.append("\"advices\" : [");
		List<RemoraAdvice> registeredAdvices = AdviceRegistry.INSTANCE.getRegisteredAdvices();
		for (int i = 0; i < registeredAdvices.size(); i++) {
			RemoraAdvice advice = registeredAdvices.get(i);
			response.append("\t{\n");
			response.append("\t\"adviceName\": ");
			response.append("\"");
			response.append(advice.getClass().getSimpleName());
			response.append("\"");
			response.append(",\n");
			List<String> configurableFields = ReflectionUtils.getConfigurableFields(advice);
			Map<String, Object> fieldsAndValues = ReflectionUtils.mapToCurrentValues(advice, configurableFields);

			response.append("\t\"properties\": {\n");
			response.append(fieldsAndValues.entrySet().stream()
					.map(entry -> "\t\t\"" + entry.getKey() + "\" : " + quote(entry.getValue()) + "")
					.collect(Collectors.joining(",\n")));

			response.append("\n\t}}");
			if (i != registeredAdvices.size() - 1) {
				response.append(",\n");
			} else {
				response.append("\n");
			}
		}
		response.append("]\n");
		response.append("}\n");
		return new RsText(response.toString());
	}
}
