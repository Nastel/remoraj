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

import javax.jms.Connection;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;

import com.ibm.mq.jms.MQConnectionFactory;
import com.ibm.mq.jms.MQQueue;
import com.ibm.msg.client.wmq.WMQConstants;

public class MQReceiveHarness extends MeasurableHarness {

	@Configurable
	public String queueManager = "T2";
	@Configurable
	public String hostName = "172.16.6.47";
	@Configurable
	public int port = 1415;
	@Configurable
	public String channel = "SYSTEM.DEF.SVRCONN";
	@Configurable
	public String user;
	@Configurable
	public String password;
	@Configurable
	public Destination destinationType = Destination.QUEUE;
	@Configurable
	public String destination;
	private MessageConsumer consumer;
	@Configurable
	public long receiveTimeout = 100L;

	@Override
	String call_() throws Exception {
		TextMessage message = (TextMessage) consumer.receive(receiveTimeout);
		if (message != null) {
			message.acknowledge();
		}
		return message == null ? "No message" : message.getText();
	}

	@Override
	public void setup() throws Exception {

		MQConnectionFactory factory = new MQConnectionFactory();
		factory.setTransportType(WMQConstants.WMQ_CM_CLIENT);

		factory.setQueueManager(queueManager);
		factory.setHostName(hostName);
		factory.setPort(port);
		factory.setChannel(channel);
		Connection connection = factory.createConnection();
		Session session = connection.createSession();
		MQQueue queue = new MQQueue(queueManager, destination);
		consumer = session.createConsumer(queue);

	}

	public enum Destination {
		TOPIC, QUEUE
	}
}
