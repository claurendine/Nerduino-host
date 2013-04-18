/*
 Part of the Nerduino IOT project - http://nerduino.com

 Copyright (c) 2013 Chase Laurendine

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software Foundation,
 Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package com.nerduino.webhost;

import org.eclipse.jetty.websocket.api.WebSocketAdapter;

public class NerduinoSocket extends WebSocketAdapter
{
	/*
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
	*/
}
