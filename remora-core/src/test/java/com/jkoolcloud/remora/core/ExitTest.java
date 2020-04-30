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

package com.jkoolcloud.remora.core;

public class ExitTest {

	public static Exit getTestExit() {
		Exit exit = new Exit();
		exit.id = JUGFactoryImpl.newUUID();
		exit.name = "TESTName";
		exit.resource = "http://localhost/test";
		exit.resourceType = EntryDefinition.ResourceType.NETADDR;
		exit.application = "JUnit";
		exit.properties.put("Property  1", "TEST1");
		exit.properties.put("Property  2", "TEST2");
		exit.properties.put("Property  3", "TEST3");
		exit.properties.put("Property  4", "TEST4");
		exit.eventType = EntryDefinition.EventType.CALL;
		exit.exception = null;
		exit.correlator = null;
		exit.exceptionTrace = null;
		return exit;
	}

}