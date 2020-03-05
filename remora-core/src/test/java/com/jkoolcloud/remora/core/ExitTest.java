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

public class ExitTest {

	public static Exit getTestExit() {
		Exit exit = new Exit();
		exit.id = new JUGFactoryImpl().newUUID();
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