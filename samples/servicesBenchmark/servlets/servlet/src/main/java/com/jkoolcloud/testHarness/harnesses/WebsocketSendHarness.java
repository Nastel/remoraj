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

import java.net.URI;
import java.util.Collections;

import javax.websocket.*;

@ClientEndpoint(encoders = { WebsocketSendHarness.class }, decoders = { WebsocketSendHarness.class })
public class WebsocketSendHarness extends MeasurableHarness implements Decoder.Text<String>, Encoder.Text<String> {

	private RemoteEndpoint.Basic basicRemote;

	@Configurable
	public String messageBody = "Hello";

	@Configurable
	public String wsEndpont = "ws://localhost:8080/wsEndpoint";

	@Override
	String call_() throws Exception {
		basicRemote.sendText(messageBody);
		return "Sent";
	}

	@Override
	public void setup() throws Exception {
		WebSocketContainer webSocketContainer = ContainerProvider.getWebSocketContainer();
		ClientEndpointConfig config = ClientEndpointConfig.Builder.create()
				.decoders(Collections.singletonList(WebsocketSendHarness.class)).build();
		Session session = webSocketContainer.connectToServer(this, new URI(wsEndpont));
	}

	@Override
	public String decode(String s) throws DecodeException {
		return s;
	}

	@Override
	public boolean willDecode(String s) {
		return true;
	}

	@Override
	public void init(EndpointConfig config) {

	}

	@Override
	public void destroy() {

	}

	@OnOpen
	public void onOpen(Session session, EndpointConfig config) {
		basicRemote = session.getBasicRemote();
	}

	@Override
	public String encode(String object) throws EncodeException {
		return object;
	}
}
