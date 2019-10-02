package com.jkoolcloud.remora.core;
/*
 * Copyright 2014-2019 JKOOL, LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.fasterxml.uuid.EthernetAddress;
import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.TimeBasedGenerator;

/**
 * Default UUID factory based on FasterXML UUID generator.
 * See: http://wiki.fasterxml.com/JugHome
 *
 * @version $Revision: 1 $
 */
public class JUGFactoryImpl {

	private static TimeBasedGenerator uuidGenerator;

	static {
		EthernetAddress nic = EthernetAddress.fromInterface();
		uuidGenerator = Generators.timeBasedGenerator(nic);
	}

	public String newUUID() {
		return uuidGenerator.generate().toString();
	}

    public String newUUID(Object obj) {
		return uuidGenerator.generate().toString();
	}
}
