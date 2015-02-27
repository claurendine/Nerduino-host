/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nerduino.library;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author chaselaurendine
 */
public class FamilyBase
{
	public FamilyBase()
	{
		
	}
	
	public String getFamilyType()
	{
		return "";
	}

	public NerduinoBase CreateNerduino()
	{
		return null;
	}

	public void readXML(Element node)
	{
	}

	public void writeXML(Document doc, Element node)
	{		
	}
	
}
