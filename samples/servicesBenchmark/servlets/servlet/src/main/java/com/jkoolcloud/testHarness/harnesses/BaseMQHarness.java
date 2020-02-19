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
import javax.jms.Session;

import com.ibm.mq.jms.MQConnectionFactory;
import com.ibm.msg.client.wmq.WMQConstants;

public class BaseMQHarness extends MeasurableHarness {
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
	public MQReceiveHarness.Destination destinationType = MQReceiveHarness.Destination.QUEUE;
	@Configurable
	public String destination;

	protected Session session;

	@Override
	String call_() throws Exception {
		return null;
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
		connection.start();
		session = connection.createSession();

	}

	public enum Destination {
		TOPIC, QUEUE
	}
}
