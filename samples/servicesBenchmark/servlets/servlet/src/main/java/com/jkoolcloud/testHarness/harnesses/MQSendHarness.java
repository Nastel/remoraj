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

package com.jkoolcloud.testHarness.harnesses;

import static java.text.MessageFormat.format;

import javax.jms.MessageProducer;
import javax.jms.TextMessage;

import com.ibm.mq.jms.MQQueue;

public class MQSendHarness extends BaseMQHarness {

	private MessageProducer producer;
	@Configurable
	public String messageBody;

	@Override
	String call_() throws Exception {
		TextMessage textMessage = session.createTextMessage(messageBody);
		producer.send(textMessage);
		return format("Sent textMessage {0}", textMessage.getJMSMessageID());
	}

	@Override
	public void setup() throws Exception {
		super.setup();
		MQQueue queue = new MQQueue(queueManager, destination);
		producer = session.createProducer(queue);
	}
}
