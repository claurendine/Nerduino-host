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

package com.nerduino.nodes;

import java.awt.Image;
import java.util.ArrayList;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.Utilities;


@SuppressWarnings({"deprecation", "rawtypes"})
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
