package com.nerduino.webhost;

import org.eclipse.jetty.websocket.server.WebSocketHandler;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

public class NerduinoWebSocketHandler extends WebSocketHandler
{
	@Override
	public void configure(WebSocketServletFactory wssf)
	{
		wssf.register(DataLinkWebSocket.class);
		wssf.register(NerduinoSocket.class);
	}
}
