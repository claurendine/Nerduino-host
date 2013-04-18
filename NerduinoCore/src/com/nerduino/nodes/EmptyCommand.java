package com.nerduino.nodes;

import java.awt.Component;
import java.awt.Dimension;

public class EmptyCommand extends Component
{
	public static int m_height = 22;
	
	public EmptyCommand()
	{
		setMaximumSize(new Dimension(120, m_height));
		setMinimumSize(new Dimension(4, 1));
		setPreferredSize(new Dimension(60, m_height));
	}
}
