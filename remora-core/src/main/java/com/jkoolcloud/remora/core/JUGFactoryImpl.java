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
