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

import java.util.Arrays;

import org.junit.Test;
import org.takes.rq.RqFake;
import org.takes.rs.RsPrint;

import com.jkoolcloud.remora.filters.AdviceFilter;
import com.jkoolcloud.remora.filters.ClassNameFilter;
import com.jkoolcloud.remora.filters.FilterManager;

public class TKFiltersTest {

	@Test
	public void act() throws Exception {
		ClassNameFilter filter = new ClassNameFilter();
		filter.classNames = Arrays.asList(new String[] { "a.b.c", "a.c.b", "c.b.a" });
		filter.mode = AdviceFilter.Mode.INCLUDE;
		FilterManager.INSTANCE.add("TESTFILTER1", filter);
		FilterManager.INSTANCE.add("TESTFILTER2", new ClassNameFilter());
		String s = new RsPrint(new TKFilters().act(new RqFake())).printBody();
		// new ObjectMapper().readTree(s);
		System.out.println(s);
	}
}