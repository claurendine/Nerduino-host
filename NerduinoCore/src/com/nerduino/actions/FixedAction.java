package com.nerduino.actions;

public class FixedAction extends StandardAction
{
	// Constructors
	public FixedAction(String label, String resourcePath)
	{
		super(label, resourcePath);
		
		this.putValue("Fixed", true);
	}
}
