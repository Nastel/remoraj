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

package com.jkoolcloud.testHarness.endpoints;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.websocket.*;
import javax.websocket.server.ServerApplicationConfig;
import javax.websocket.server.ServerEndpointConfig;

public class WebsocketEchoEndpoint extends Endpoint implements ServerApplicationConfig {

	@Override
	public void onOpen(Session session, EndpointConfig config) {
		System.out.println("Peer " + session.getId() + " connected");
		session.addMessageHandler(new MessageHandler.Whole<String>() {
			@Override
			public void onMessage(String message) {
				try {
					session.getBasicRemote().sendText("Got message from " + session.getId() + "\n" + message);
				} catch (IOException ex) {
				}
			}
		});
	}

	@Override
	public void onClose(Session session, CloseReason closeReason) {
		System.out.println("Peer " + session.getId() + " disconnected due to " + closeReason.getReasonPhrase());
	}

	@Override
	public void onError(Session session, Throwable error) {
		System.out.println("Error communicating with peer " + session.getId() + ". Detail: " + error.getMessage());
	}

	@Override
    public Set<ServerEndpointConfig> getEndpointConfigs(Set<Class<? extends Endpoint>> endpointClasses) {
		Set<ServerEndpointConfig> result = new HashSet<>();
		for (Class epClass : endpointClasses) {
			// need to ignore Client endpoint class
			if (epClass.equals(WebsocketEchoEndpoint.class)) {
				ServerEndpointConfig sec = ServerEndpointConfig.Builder.create(epClass, "/wsEndpoint").build();
				result.add(sec);
			}
		}
		return result;
	}

	@Override
    public Set<Class<?>> getAnnotatedEndpointClasses(Set<Class<?>> scanned) {
		return Collections.emptySet();
	}
}
