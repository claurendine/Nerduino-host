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

package com.nerduino.library;

public enum ResponseStatusEnum 
{
	    RS_Complete(0),
		RS_Pending(1),
	    RS_Timeout(2),
	    RS_CommandNotRecognized(3),
		RS_ResponseOverflow(4),
		RS_Failed(5),
		RS_Undeliverable(6),
		RS_TypeMismatch(7),
		RS_UndefinedResponse(8),
		RS_PartialResult(9);

		
	    private final byte value;
	    
	    ResponseStatusEnum(int val)
	    {
	    	this.value = (byte)val;
	    }
	    
	    public byte Value()
	    {
	    	return this.value;
	    }

		public static ResponseStatusEnum valueOf(byte b) 
		{
			switch(b)
			{
				case 0:
					return RS_Complete;
				case 1:
					return RS_Pending;
				case 2:
					return RS_Timeout;
				case 3:
					return RS_CommandNotRecognized;
				case 4:
					return RS_ResponseOverflow;
				case 5:
					return RS_Failed;
				case 6:
					return RS_Undeliverable;
				case 7:
					return RS_TypeMismatch;
				case 8:
					return RS_UndefinedResponse;
				case 9:
					return RS_PartialResult;
			}	
			
			return null;
		}
}