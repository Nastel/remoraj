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

public class TkAdviceList implements Take {

	@Override
	public Response act(Request req) throws Exception {
		StringBuilder response = new StringBuilder();
		response.append("{\n");
		response.append("\"remoraJVersion\" : \"" + Remora.getVersion() + "\",\n");
		response.append("\"vmIdentification\" : \"" + System.getProperty(Remora.REMORA_VM_IDENTIFICATION) + "\",\n");
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
			List<String> configurableFields = AdviceRegistry.getConfigurableFields(advice);
			Map<String, String> fieldsAndValues = AdviceRegistry.mapToCurrentValues(advice, configurableFields);
			response.append("\t\"properties\": {\n");
			response.append(fieldsAndValues.entrySet().stream()
					.map(entry -> "\t\t\"" + entry.getKey() + "\" : \"" + entry.getValue() + "\"")
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
