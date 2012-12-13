package com.nerduino.nodes;

import java.awt.Image;
import java.util.ArrayList;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.Utilities;


public class RootNode extends AbstractNode 
{
	ArrayList m_nodes = new ArrayList();
	
    public RootNode(Children children) 
	{
        super(children);
    }

	public RootNode()
	{
		super(new Children.Array());
	}
    
	@Override
    public Image getIcon(int type) 
	{
        return Utilities.loadImage("org/netbeans/myfirstexplorer/right-rectangle.png");
    }
    
	@Override
    public Image getOpenedIcon(int type) 
	{
        return Utilities.loadImage("org/netbeans/myfirstexplorer/down-rectangle.png");
    }
    
}
