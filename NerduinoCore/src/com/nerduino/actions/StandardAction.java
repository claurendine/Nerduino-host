package com.nerduino.actions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

public class StandardAction extends AbstractAction
{
	// Declarations
	private ImageIcon m_icon;
	private String m_label;
        
	// Constructors
	public StandardAction(String label, String resourcePath) 
	{
		super("");

		m_label = label;

		java.net.URL imgURL = getClass().getResource(resourcePath);

		if (imgURL != null)
			m_icon = new ImageIcon(imgURL);

		this.putValue("Fixed", false);
		
		update();
	}

	// Methods
	public void update()
	{
		try
		{
			putValue(SMALL_ICON, m_icon);
			putValue(NAME, m_label);
			putValue(SHORT_DESCRIPTION, m_label);
		}
		catch(Exception e)
		{
		}
	}

	public void actionPerformed(ActionEvent e) 
	{
	}
}
