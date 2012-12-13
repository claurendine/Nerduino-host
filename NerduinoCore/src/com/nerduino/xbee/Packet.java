package com.nerduino.xbee;

public class Packet 
{
    public byte RSSI;
    public byte Options;
    public int AddressHigh;
    public int AddressLow;
    public short AddressSource;
    
    public byte[] Data;
}
