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

package processing.app;

import com.nerduino.nodes.TreeNode;
import org.openide.nodes.Children;
import org.openide.nodes.Sheet;

public class Board extends TreeNode
{
	String m_shortName;
	String m_processor;
	String m_uploadTool;
	String m_uploadProtocol;
	int m_uploadMaxSize;
	int m_uploadSpeed;
	short m_bootloaderLowFuses;
	short m_bootloaderHighFuses;
	short m_bootloaderExtendedFuses;
	String m_bootloaderPath;
	String m_bootloaderTool;
	String m_bootloaderFile;
	short m_bootloaderUnlockBits;
	short m_bootloaderLockBits;
	String m_buildMCU;
	long m_buildF_CPU;
	String m_buildCore;
	String m_buildVariant;
	
	public Board()
	{
		super(new Children.Array(), "Boards", "/com/nerduino/resources/Board16.png");
		
		m_hasEditor = false;
	}

	public Board(Board clone)
	{
		super(new Children.Array(), "Boards", "/com/nerduino/resources/Board16.png");
		
		m_hasEditor = false;
		
		m_name = clone.m_name;
		m_shortName = clone.m_shortName;
		m_processor = clone.m_processor;
		m_uploadTool = clone.m_uploadTool;
		m_uploadProtocol = clone.m_uploadProtocol;
		m_uploadMaxSize = clone.m_uploadMaxSize;
		m_uploadSpeed = clone.m_uploadSpeed;
		m_bootloaderLowFuses = clone.m_bootloaderLowFuses;
		m_bootloaderHighFuses = clone.m_bootloaderHighFuses;
		m_bootloaderExtendedFuses = clone.m_bootloaderExtendedFuses;
		m_bootloaderPath = clone.m_bootloaderPath;
		m_bootloaderTool = clone.m_bootloaderTool;
		m_bootloaderFile = clone.m_bootloaderFile;
		m_bootloaderUnlockBits = clone.m_bootloaderUnlockBits;
		m_bootloaderLockBits = clone.m_bootloaderLockBits;
		m_buildMCU = clone.m_buildMCU;
		m_buildF_CPU = clone.m_buildF_CPU;
		m_buildCore = clone.m_buildCore;
		m_buildVariant = clone.m_buildVariant;
	}

	@Override
	public String getName()
	{
		if (m_processor != null && m_processor.length() > 0)
			return m_name + " / " + m_processor;
		
		return m_name;
	}
	
	public String getShortName()
	{
		return m_shortName;
	}
	
	public void setShortName(String val)
	{
		m_shortName = val;
	}

	public String getProcessor()
	{
		return m_processor;
	}
	
	public void setProcessor(String val)
	{
		m_processor = val;
	}

	
	public String getUploadProtocol()
	{
		return m_uploadProtocol;
	}
	
	public void setUploadProtocol(String val)
	{
		m_uploadProtocol = val;
	}
	
	public String getUploadTool()
	{
		return m_uploadTool;
	}
	
	public void setUploadTool(String val)
	{
		m_uploadTool = val;
	}
	
	public int getUploadMaxSize()
	{
		return m_uploadMaxSize;
	}
	
	public void setUploadMaxSize(int val)
	{
		m_uploadMaxSize = val;
	}
	
	public int getUploadSpeed()
	{
		return m_uploadSpeed;
	}
	
	public void setUploadSpeed(int val)
	{
		m_uploadSpeed = val;
	}
	
	public short getBootloaderLowFuses()
	{
		return m_bootloaderLowFuses;
	}
	
	public void setBootloaderLowFuses(short val)
	{
		m_bootloaderLowFuses = val;
	}
	
	public short getBootloaderHighFuses()
	{
		return m_bootloaderHighFuses;
	}
	
	public void setBootloaderHighFuses(short val)
	{
		m_bootloaderHighFuses = val;
	}
	
	public short getBootloaderExtendedFuses()
	{
		return m_bootloaderExtendedFuses;
	}
	
	public void setBootloaderExtendedFuses(short val)
	{
		m_bootloaderExtendedFuses = val;
	}
	
	public String getBootloaderPath()
	{
		return m_bootloaderPath;
	}
	
	public void setBootloaderPath(String val)
	{
		m_bootloaderPath = val;
	}

	public String getBootloaderTool()
	{
		return m_bootloaderTool;
	}
	
	public void setBootloaderTool(String val)
	{
		m_bootloaderTool = val;
	}

	public String getBootloaderFile()
	{
		return m_bootloaderFile;
	}
	
	public void setBootloaderFile(String val)
	{
		m_bootloaderFile = val;
	}
	
	public short getBootloaderUnlockBits()
	{
		return m_bootloaderUnlockBits;
	}
	
	public void setBootloaderUnlockBits(short val)
	{
		m_bootloaderUnlockBits = val;
	}
	
	public short getBootloaderLockBits()
	{
		return m_bootloaderLockBits;
	}
	
	public void setBootloaderLockBits(short val)
	{
		m_bootloaderLockBits = val;
	}
	
	public String getBuildMCU()
	{
		return m_buildMCU;
	}
	
	public void setBuildMCU(String val)
	{
		m_buildMCU = val;
	}
	
	public long getBuildF_CPU()
	{
		return m_buildF_CPU;
	}
	
	public void setBuildF_CPU(long val)
	{
		m_buildF_CPU = val;
	}
	
	public String getBuildCore()
	{
		return m_buildCore;
	}
	
	public void setBuildCore(String val)
	{
		m_buildCore = val;
	}
	
	public String getBuildVariant()
	{
		return m_buildVariant;
	}
	
	public void setBuildVariant(String val)
	{
		m_buildVariant = val;
	}
	
	
	@Override
	public PropertySet[] getPropertySets()
	{
		final Sheet.Set sheet = Sheet.createPropertiesSet();
		
		sheet.setDisplayName("Board Information");
		
		addProperty(sheet, String.class, null, "Name", "Name");
		addProperty(sheet, String.class, null, "ShortName", "ShortName");
		addProperty(sheet, String.class, null, "Processor", "Processor");
		addProperty(sheet, String.class, null, "UploadTool", "UploadTool");
		addProperty(sheet, String.class, null, "UploadProtocol", "UploadProtocol");
		addProperty(sheet, int.class, null, "UploadMaxSize", "UploadMaxSize");
		addProperty(sheet, int.class, null, "UploadSpeed", "UploadSpeed");
		addProperty(sheet, short.class, null, "BootloaderLowFuses", "BootloaderLowFuses");
		addProperty(sheet, short.class, null, "BootloaderHighFuses", "BootloaderHighFuses");
		addProperty(sheet, short.class, null, "BootloaderExtendedFuses", "BootloaderExtendedFuses");
		addProperty(sheet, String.class, null, "BootloaderTool", "BootloaderTool");
		addProperty(sheet, String.class, null, "BootloaderPath", "BootloaderPath");
		addProperty(sheet, String.class, null, "BootloaderFile", "BootloaderFile");
		addProperty(sheet, short.class, null, "BootloaderUnlockBits", "BootloaderUnlockBits");
		addProperty(sheet, short.class, null, "BootloaderLockBits", "BootloaderLockBits");
		addProperty(sheet, String.class, null, "BuildMCU", "BuildMCU");
		addProperty(sheet, long.class, null, "BuildF_CPU", "BuildF_CPU");
		addProperty(sheet, String.class, null, "BuildCore", "BuildCore");
		addProperty(sheet, String.class, null, "BuildVariant", "BuildVariant");

		return new PropertySet[] { sheet };
	}
}