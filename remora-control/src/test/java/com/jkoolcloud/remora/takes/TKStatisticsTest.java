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

import java.util.Collections;

import org.junit.Test;
import org.takes.facets.fork.RqRegex;
import org.takes.rs.RsPrint;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jkoolcloud.remora.AdviceRegistry;
import com.jkoolcloud.remora.adviceListeners.CountingAdviceListener;
import com.jkoolcloud.remora.adviceListeners.TimingAdviceListener;
import com.jkoolcloud.remora.advices.Advice1;
import com.jkoolcloud.remora.advices.BaseTransformers;

public class TKStatisticsTest {

	@Test
	public void act() throws Exception {
		AdviceRegistry.INSTANCE.report(Collections.singletonList(new Advice1()));
		BaseTransformers.registerListener(CountingAdviceListener.class);
		BaseTransformers.registerListener(TimingAdviceListener.class);
		String s = new RsPrint(
				new TKStatistics().act(new RqRegex.Fake("/statistics/(?<advice>[^/]+)", "/statistics/Advice1")))
						.printBody();
		JsonNode jsonNode = new ObjectMapper().readTree(s);
		System.out.println(s);
	}

}