package com.nerduino.webhost;

import com.nerduino.library.Address;
import com.nerduino.library.NerduinoManager;
import com.nerduino.library.NerduinoNet;
import com.nerduino.xbee.BitConverter;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;

public class NerduinoSocket extends WebSocketAdapter
{
	NerduinoNet m_nerduino = null;
	
	@Override
	public void onWebSocketClose(int statusCode, String message)
	{
		super.onWebSocketClose(statusCode, message);
		
		// notify the nerduinonet that the socket is closed
		if (m_nerduino != null)
		{
			m_nerduino.setSocket(null);
		}
	}

	@Override
	public void onWebSocketConnect(Session session)
	{
		super.onWebSocketConnect(session);
		
		// wait for an incoming message before pairing with a nerduinonet
	}
	
	@Override
	public void onWebSocketBinary(byte[] payload, int offset, int len)
	{
		if (m_nerduino == null)
		{
			// the first message should contain address data for the remote nerduino
			if (len >= 8)
			{
				long serialNumber = BitConverter.GetLong(payload, offset);

				// lookup a nerduino with this address, if one does not exist then create a new one
				m_nerduino = NerduinoManager.Current.getNerduinoNet(serialNumber);

				if (m_nerduino == null)
				{
					m_nerduino = new NerduinoNet();
					m_nerduino.setSerialNumber(serialNumber);

					NerduinoManager.Current.addChild(m_nerduino);
				}

				// associate the nerduino with this websocket
				m_nerduino.setSocket(this);
			}
			
			return;
		}
		
		m_nerduino.processIncomingStream(payload, offset, len);
	}
}
