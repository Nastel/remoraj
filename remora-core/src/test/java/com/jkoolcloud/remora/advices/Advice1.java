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

package com.jkoolcloud.remora.advices;

import java.lang.instrument.Instrumentation;

import org.tinylog.Logger;
import org.tinylog.TaggedLogger;

import com.jkoolcloud.remora.RemoraConfig;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

public class Advice1 extends BaseTransformers {
	private static final TaggedLogger LOGGER = Logger.tag("INFO");
	private static final String ADVICE_NAME = "1";

	@RemoraConfig.Configurable
	public static String test = "TEST1";

	public Advice1() {
		LOGGER.info("Initialsed");
	}

	@Override
	public void install(Instrumentation inst) {

	}

	@Override
	public String getName() {
		return ADVICE_NAME;
	}

	@Override
	public ElementMatcher<TypeDescription> getTypeMatcher() {
		return null;
	}

	@Override
	public AgentBuilder.Transformer getAdvice() {
		return null;
	}

	@Override
	protected AgentBuilder.Listener getListener() {
		return null;
	}
}
