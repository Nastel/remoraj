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

import java.util.UUID;

import com.fasterxml.uuid.EthernetAddress;
import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.TimeBasedGenerator;

/**
 * Default UUID factory based on FasterXML UUID generator. See: http://wiki.fasterxml.com/JugHome
 *
 * @version $Revision: 1 $
 */
public class JUGFactoryImpl {

	private static final TimeBasedGenerator uuidGenerator;

	static {
		EthernetAddress nic = EthernetAddress.fromInterface();
		uuidGenerator = Generators.timeBasedGenerator(nic);
	}

	public static String newUUID() {
		return toString(uuidGenerator.generate());
	}

	public static String newUUID(Object obj) {
		return toString(uuidGenerator.generate());
	}

	public static UUID getUUID() {
		return uuidGenerator.generate();
	}

	public static final String toString(UUID uuid) {
		long msb = uuid.getMostSignificantBits();
		long lsb = uuid.getLeastSignificantBits();
		char[] uuidChars = new char[36];
		int cursor = uuidChars.length;
		while (cursor > 24) {
			cursor -= 2;
			System.arraycopy(recode[(int) (lsb & 0xff)], 0, uuidChars, cursor, 2);
			lsb >>>= 8;
		}
		uuidChars[--cursor] = '-';
		while (cursor > 19) {
			cursor -= 2;
			System.arraycopy(recode[(int) (lsb & 0xff)], 0, uuidChars, cursor, 2);
			lsb >>>= 8;
		}
		uuidChars[--cursor] = '-';
		while (cursor > 14) {
			cursor -= 2;
			System.arraycopy(recode[(int) (msb & 0xff)], 0, uuidChars, cursor, 2);
			msb >>>= 8;
		}
		uuidChars[--cursor] = '-';
		while (cursor > 9) {
			cursor -= 2;
			System.arraycopy(recode[(int) (msb & 0xff)], 0, uuidChars, cursor, 2);
			msb >>>= 8;
		}
		uuidChars[--cursor] = '-';
		while (cursor > 0) {
			cursor -= 2;
			System.arraycopy(recode[(int) (msb & 0xff)], 0, uuidChars, cursor, 2);
			msb >>>= 8;
		}
		return new String(uuidChars);
	}

	private static final char[][] recode = buildByteBlocks();

	private static char[][] buildByteBlocks() {
		char[][] ret = new char[256][];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = String.format("%02x", i).toCharArray();
		}
		return ret;
	}

}
