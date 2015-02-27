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

package com.nerduino.xbee;

//import com.nerduino.library.XBeeManager;

public class ZigbeeFrameWithResponse extends ZigbeeFrame
{
	// Declarations
	public Boolean AutoGenerateFrameID = true;
	public byte FrameID;
	public Boolean AutoRelease = true;
	
    // Constructors
    public ZigbeeFrameWithResponse(FrameTypeEnum frameType)
    {
        super(frameType);
    }

    // Methods
    public int Send()
    {
//        if (AutoGenerateFrameID)
//            FrameID = XBeeManager.getNextFrameID();

//        XBeeManager.reserveFrameID(this, FrameID);

        return super.Send();
    }

    public int Send(byte frameID)
    {
        FrameID = frameID;
        
        return super.Send();
    }

    public void Release()
    {
//        XBeeManager.releaseFrameID(FrameID);
    }

    public void OnResponse(byte[] data)
    {
        if (AutoRelease)
            Release();
        
        /*
        if (Response != null)
        {
            Response(data, EventArgs.Empty);
        }
        */
    }

	
}
