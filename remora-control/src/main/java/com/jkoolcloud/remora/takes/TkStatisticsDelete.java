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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.takes.Request;
import org.takes.Response;
import org.takes.Take;
import org.takes.facets.fork.RqRegex;
import org.takes.rs.RsText;

import com.jkoolcloud.remora.AdviceRegistry;
import com.jkoolcloud.remora.advices.AdviceListener;

public class TkStatisticsDelete implements Take {
	@Override
	public Response act(Request req) throws Exception {

		String advice = ((RqRegex) req).matcher().group("advice");
		if (advice == null) {
			throw new IllegalArgumentException();
		}
		try {
			Class<?> aClass = Class.forName("com.jkoolcloud.remora.advices." + advice);
			List<AdviceListener> listeners = AdviceRegistry.INSTANCE
					.getBaseTransformerByName(aClass.getSimpleName()).listeners;
			List<? extends AdviceListener> newAdviceListeners = listeners.stream().map(l -> l.getClass())
					.map(aClass1 -> {
						try {
							return aClass1.newInstance();
						} catch (InstantiationException e) {
							new RsText("ERROR: " + e.getClass().getSimpleName() + " " + e);
						} catch (IllegalAccessException e) {
							new RsText("ERROR " + e.getClass().getSimpleName() + " " + e);
						}
						return null;
					}).filter(l -> l != null).collect(Collectors.toList());
			AdviceRegistry.INSTANCE.getBaseTransformerByName(aClass.getSimpleName()).listeners = new ArrayList<>(10);
			AdviceRegistry.INSTANCE.getBaseTransformerByName(aClass.getSimpleName()).listeners
					.addAll(newAdviceListeners);

		} catch (Exception e) {

		}

		return new RsText("OK");

	}
}
