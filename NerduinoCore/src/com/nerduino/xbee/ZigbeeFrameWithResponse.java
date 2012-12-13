package com.nerduino.xbee;

public class ZigbeeFrameWithResponse extends ZigbeeFrame
{
	// Declarations
	public Boolean AutoGenerateFrameID = true;
	public byte FrameID;
	public Boolean AutoRelease = true;
	
	/*
    // Events
    public event EventHandler Response;
    */
	
    // Constructors
    public ZigbeeFrameWithResponse(FrameTypeEnum frameType, SerialBase parent)
    {
        super(frameType, parent);
    }

    // Methods
    public int Send()
    {
        if (AutoGenerateFrameID)
            FrameID = Parent.getNextFrameID();

        Parent.reserveFrameID(this, FrameID);

        return super.Send();
    }

    public int Send(byte frameID)
    {
        FrameID = frameID;
        
        return super.Send();
    }

    public void Release()
    {
        Parent.releaseFrameID(FrameID);
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
