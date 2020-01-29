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

package com.jkoolcloud.remora;

import org.junit.Test;
import org.tinylog.configuration.Configuration;

public class RemoraInitTest {

	@Test
	public void configureLogger() {
		System.setProperty(Remora.REMORA_PATH, ".");
		RemoraInit init = new RemoraInit();
		// Remora.configureRemoraRootLogger(".");
		// init.configureAdviceLogger(new Advice1());
		// init.configureAdviceLogger(new Advice2());

	}

	public static void main(String[] args) {
		String key = "writer" + "NO";
		String adviceName = "NO";

		org.tinylog.Logger.tag("INIT").info("HI");
		Configuration.set(key, "rolling file");
		Configuration.set(key + ".file", adviceName + ".log");
		Configuration.set(key + ".format", " {level}: {message}");
		Configuration.set(key + ".tag", adviceName);
		Configuration.set(key + ".level", "debug");
		org.tinylog.Logger.tag("NO").info("HI");
		org.tinylog.Logger.tag("ED").info("ED");

		org.tinylog.Logger.info("HI");
	}

}
