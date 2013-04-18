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

package com.nerduino.processing.app;

import com.nerduino.nodes.TreeNode;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

public class BoardManager extends TreeNode
{
	// declarations
	public static BoardManager Current;
	
	private Children m_nodes;
	
	// Constructors
	public BoardManager()
	{
		super(new Children.Array(), "Boards", "/com/nerduino/resources/BoardManager16.png");
		
		m_nodes = this.getChildren();
		m_hasEditor = false;
		
		Current = this;		
	}
	
	// Properties
	public void readBoardDefinitions()
	{
		//m_nodes.clear();
		
		String path = ArduinoManager.Current.getArduinoPath() + "/Source/Arduino/hardware/arduino/boards.txt";

		File file = new File(path);

		if (file.exists())
		{
			try
			{
				FileReader fr = new FileReader(path);
				BufferedReader in = new BufferedReader(fr);
				String line;
				String boardname = "";
				Board board = null;

				while ((line = in.readLine()) != null)   
				{
					line = line.trim();
					
					if (line.length() > 0 && !line.startsWith("#"))
					{
						int i = line.indexOf(".");
						
						if (i > 0)
						{
							String bn = line.substring(0, i);
							
							if (!boardname.equals(bn))
							{
								board = new Board();
								board.setShortName(bn);
								
								boardname = bn;
								
								this.addBoard(board);
								//m_nodes.add(board);
							}
							
							line = line.substring(i+1);
							
							i = line.indexOf("=");
							
							if (i > 0)
							{
								String token = line.substring(0, i);
								String value = line.substring(i + 1);
								
								if (token.equals("name"))
									board.setName(value);
								else if (token.equals("upload.protocol"))
									board.setUploadProtocol(value);
								else if (token.equals("upload.maximum_size"))
									board.setUploadMaxSize(Integer.decode(value));
								else if (token.equals("upload.speed"))
									board.setUploadSpeed(Integer.decode(value));
								else if (token.equals("bootloader.low_fuses"))
								{
									if (value.startsWith("0x") || value.startsWith("0X"))
										value = value.substring(2);
									
									BigInteger bi = new BigInteger(value, 16);

									board.setBootloaderLowFuses(bi.shortValue());
								}
								else if (token.equals("bootloader.high_fuses"))
								{
									if (value.startsWith("0x") || value.startsWith("0X"))
										value = value.substring(2);
									
									BigInteger bi = new BigInteger(value, 16);

									board.setBootloaderHighFuses(bi.shortValue());
								}
								else if (token.equals("bootloader.extended_fuses"))
								{
									if (value.startsWith("0x") || value.startsWith("0X"))
										value = value.substring(2);
									
									BigInteger bi = new BigInteger(value, 16);

									board.setBootloaderExtendedFuses(bi.shortValue());
								}
								else if (token.equals("bootloader.path"))
									board.setBootloaderPath(value);
								else if (token.equals("bootloader.file"))
									board.setBootloaderFile(value);
								else if (token.equals("bootloader.unlock_bits"))
								{
									if (value.startsWith("0x") || value.startsWith("0X"))
										value = value.substring(2);
									
									BigInteger bi = new BigInteger(value, 16);

									board.setBootloaderUnlockBits(bi.shortValue());
								}
								else if (token.equals("bootloader.lock_bits"))
								{
									if (value.startsWith("0x") || value.startsWith("0X"))
										value = value.substring(2);
									
									BigInteger bi = new BigInteger(value, 16);

									board.setBootloaderLockBits(bi.shortValue());
								}
								else if (token.equals("build.mcu"))
									board.setBuildMCU(value);
								else if (token.equals("build.f_cpu"))
								{
									if (value.endsWith("L"))
										value = value.substring(0, value.length() - 1);
									
									board.setBuildF_CPU(Long.decode(value));
								}
								else if (token.equals("build.core"))
									board.setBuildCore(value);
								else if (token.equals("build.variant"))
									board.setBuildVariant(value);
								
							}
						}
					}
				}

				fr.close();
			}
			catch(IOException ex)
			{
				//Exceptions.printStackTrace(ex);
			}
		}
	}

	public Object[] getDeviceList()
	{
		ArrayList<String> list = new ArrayList<String>();

		Node[] nodes = m_nodes.getNodes();
		
		for(Node n : nodes)
		{
			Board board = (Board) n;

			list.add(board.getName());
		}
		
		return list.toArray();
	}
	
	public void addBoard(Board board)
	{
		if (board != null && !contains(board))
		{
			org.openide.nodes.Node[] nodes = new org.openide.nodes.Node[1];
			nodes[0] = board;
			
			m_nodes.add(nodes);
		}
	}
	
	public void removeBoard(Board board)
	{
		if (board != null && contains(board))
		{
			Node[] nodes = new Node[1];
			nodes[0] = board;

			m_nodes.remove(nodes);
		}	
	}
	
	public boolean contains(Board board)
	{
		for(int i = 0; i < m_nodes.getNodesCount(); i++)
		{
			Board node = (Board) m_nodes.getNodeAt(i);
		
			if (node == board)
				return true;
		}
		
		return false;
	}

	public Board createNewBoard()
	{
		Board nu = new Board();
		
		nu.setName(getUniqueName(nu.getName()));
		
		/*
		// show the configure dialog
		BoardConfigDialog dialog = new BoardConfigDialog(new javax.swing.JFrame(), true);
		
		dialog.setBoard(nu);
		dialog.setVisible(true);
		
		nu = dialog.m_board;

		nu.setName(getUniqueName(nu.getName()));
		*/
		
		if (nu != null)
			addBoard(nu);
		
		return nu;
	}

	public Board getBoard(String boardType)
	{
		for(int i = 0; i < m_nodes.getNodesCount(); i++)
		{
			Board board = (Board) m_nodes.getNodeAt(i);
		
			if (board.getName().matches(boardType))
				return board;
		}

		return null;
	}
}
