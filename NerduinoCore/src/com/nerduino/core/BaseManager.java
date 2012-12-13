/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nerduino.core;

import com.nerduino.nodes.TreeNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


public class BaseManager extends TreeNode
{
	// Declarations
	protected Children m_children;
	//String m_path = "/Users/chaselaurendine/Documents/Nerduino/Services";

	public BaseManager(String name, String icon)
	{
		super(new Children.Array(), name, icon);
		
		m_children = this.getChildren();
		
		m_hasEditor = false;
	}
	
	protected TreeNode createNewChild()
	{
		return null;
	}
	
	public final TreeNode createNew()
	{
		TreeNode newnode = createNewChild();
		
		if (newnode != null)
		{
			if (configureChild(newnode))
			{
				addChild(newnode);

				newnode.select();
			}
		}
		
		return newnode;
	}
	
	public boolean configureChild(TreeNode child)
	{
		child.setName(getUniqueName(child.getName()));
		
		return true;
	}
	
	public boolean contains(TreeNode node)
	{
		for(int i = 0; i < m_children.getNodesCount(); i++)
		{
			TreeNode tn = (TreeNode) m_children.getNodeAt(i);
		
			if (node == tn)
				return true;
		}
		
		return false;
	}
	
	public void addChild(TreeNode node)
	{
		if (node != null && !contains(node))
		{
			org.openide.nodes.Node[] nodes = new org.openide.nodes.Node[1];
			nodes[0] = node;
			
			m_children.add(nodes);

			saveConfiguration();
		}
	}
	
	public void removeChild(TreeNode node)
	{
		if (node != null && contains(node))
		{
			Node[] nodes = new Node[1];
			nodes[0] = node;
			
			m_children.remove(nodes);
			
			saveConfiguration();
		}
	}
	
	public void saveConfiguration()
	{
		if (AppManager.Current != null)
			AppManager.Current.saveConfiguration();	
	}

	
	@Override
	public void readXML(Element elem)
	{
		NodeList nl = elem.getElementsByTagName(getName());
		
		if (nl != null && nl.getLength() > 0)
		{
			NodeList nodes = ((Element) nl.item(0)).getElementsByTagName("Node");
			
			if (nodes != null && nodes.getLength() > 0)
			{
				for(int i = 0; i < nodes.getLength(); i++)
				{
					TreeNode newnode = createNewChild();
					
					newnode.readXML((Element) nodes.item(i));
					
					addChild(newnode);
				}
			}
		}

	}

	@Override
	public void writeXML(Document doc, Element elem)
	{
		Element element = doc.createElement(getName());
		
		for(Node node : m_children.getNodes())
		{
			((TreeNode) node).writeXML(doc, element);
		}
		
		elem.appendChild(element);
	}
	
	public String getFilePath()
	{
		return null;
	}

}
