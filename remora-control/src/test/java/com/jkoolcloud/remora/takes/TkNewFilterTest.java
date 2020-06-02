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

import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.takes.rq.RqFake;
import org.takes.rq.RqWithBody;
import org.tinylog.Logger;

import com.jkoolcloud.remora.filters.ClassNameFilter;

public class TkNewFilterTest {
	public static final String TEST_BODY = "{\n" + "\t\"class\": \"" + ClassNameFilter.class.getName() + "\",\n"//
			+ "\t\"name\": \"test\",\n"//
			+ "\t\"regex\": \"true\",\n"//
			+ "\t\"classNames\": \"test;test2\",\n"//
			+ "\t\"mode\": \"INCLUDE\"\n" + "}\n";

	@Test
	public void testFormatResponse() throws Exception {
		TkNewFilter tkNewFilter = new TkNewFilter(Logger.tag("TEST"));
		@NotNull
		String jsonInString = TakesUtils.getBody(tkNewFilter.act(new RqWithBody(new RqFake(), TEST_BODY)).body());

		System.out.println(jsonInString);
	}

	// @Test
	// public void testgetValueForKey() throws ParseException {
	// assertEquals("Advice1", TkChange.getValueForKey("advice", TEST_BODY));
	// assertEquals("test", TkChange.getValueForKey("property", TEST_BODY));
	// assertEquals("test", TkChange.getValueForKey("value", TEST_BODY));
	// }
}