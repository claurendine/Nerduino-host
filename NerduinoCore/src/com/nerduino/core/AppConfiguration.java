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

package com.nerduino.core;

import java.io.File;
import org.openide.util.Exceptions;
import org.tmatesoft.sqljet.core.SqlJetException;
import org.tmatesoft.sqljet.core.SqlJetTransactionMode;
import org.tmatesoft.sqljet.core.table.ISqlJetCursor;
import org.tmatesoft.sqljet.core.table.ISqlJetOptions;
import org.tmatesoft.sqljet.core.table.ISqlJetTable;
import org.tmatesoft.sqljet.core.table.ISqlJetTransaction;
import org.tmatesoft.sqljet.core.table.SqlJetDb;

public class AppConfiguration
{
	public static AppConfiguration Current = null;
	SqlJetDb m_db;
	ISqlJetTable m_paramTable;
	ISqlJetTable m_pointTable;

	
	public AppConfiguration(String filename)
	{
		Current = this;

		File dbFile = new File(filename);
			
		try
		{
			if (!dbFile.exists())
			{
				m_db = SqlJetDb.open(dbFile, true);
				ISqlJetOptions opts =  m_db.getOptions();

				opts.setAutovacuum(true);
				m_db.runTransaction(new ISqlJetTransaction() 
					{
						public Object run(SqlJetDb db) throws SqlJetException 
						{
							m_db.getOptions().setUserVersion(1);
							return true;
						}
					}, SqlJetTransactionMode.WRITE);
				
				m_db.close();
			}

			m_db = SqlJetDb.open(dbFile, true);
			
			try
			{
				m_paramTable = m_db.getTable("ParamTable");
			}
			catch(SqlJetException ex)
			{
				String createQuery = "CREATE TABLE ParamTable (param TEXT NOT NULL PRIMARY KEY, value TEXT NOT NULL)";
				String indexQuery = "CREATE INDEX paramIndex ON ParamTable (param)";
				
				m_db.createTable(createQuery);
				m_db.createIndex(indexQuery);
				
				m_paramTable = m_db.getTable("ParamTable");
			}
			
			try
			{
				m_pointTable = m_db.getTable("PointTable");
			}
			catch(SqlJetException ex)
			{
				String createQuery = "CREATE TABLE PointTable (point TEXT NOT NULL PRIMARY KEY, type TEXT, value TEXT NOT NULL)";
				String indexQuery = "CREATE INDEX pointIndex ON PointTable (point)";
				
				m_db.createTable(createQuery);
				m_db.createIndex(indexQuery);
				
				m_pointTable = m_db.getTable("PointTable");
			}
		}
		catch(SqlJetException ex)
		{
			Exceptions.printStackTrace(ex);
		}
	}
	
	public String getParameter(String key)
	{
		try
		{
			m_db.beginTransaction(SqlJetTransactionMode.READ_ONLY);
			
			ISqlJetCursor cursor = m_paramTable.lookup("paramIndex", key);

			String val = cursor.getString("value");
			
			cursor.close();
			
			return val;
		}
		catch(SqlJetException ex)
		{
			//Exceptions.printStackTrace(ex);
		}
		finally
		{
			try
			{
				m_db.commit();
			}
			catch(SqlJetException ex)
			{
			}
		}
		
		return null;
	}
	
	public String getParameter(String key, String defaultValue)
	{
		String ret = getParameter(key);
		
		return  (ret == null) ? defaultValue : ret;
	}
	
	public void setParameter(String key, String value)
	{
		if (m_db != null && m_paramTable != null)
		{
			try
			{
				try
				{
					m_db.beginTransaction(SqlJetTransactionMode.WRITE);

					m_paramTable.insert(key, value);

					m_db.commit();
				}
				catch(Exception e)
				{
					m_db.beginTransaction(SqlJetTransactionMode.WRITE);

					ISqlJetCursor updateCursor =  m_paramTable.open();

					do
					{
						if (key.equals(updateCursor.getValue("param")))
						{
							updateCursor.update(key, value);
						}
				    } while(updateCursor.next());

					updateCursor.close();
					
					
					m_db.commit();
				}	
			}	
			catch(Exception e)
			{
				int i = 0;
			}
		}
	}
	
	public String[] getList(String key)
	{		
		try
		{
			m_db.beginTransaction(SqlJetTransactionMode.READ_ONLY);
			
			ISqlJetCursor cursor = m_paramTable.lookup("paramIndex", key);

			String val = cursor.getString("value");
			
			String[] lval = val.split(",");
			
			cursor.close();
			
			return lval;
		}
		catch(SqlJetException ex)
		{
			//Exceptions.printStackTrace(ex);
		}
		finally
		{
			try
			{
				m_db.commit();
			}
			catch(SqlJetException ex)
			{
			}
		}
		
		return null;
	}
	
	public void setList(String key, String[] list)
	{
		if (m_db != null && m_paramTable != null)
		{
			StringBuilder sb = new StringBuilder();
			
			if (list != null && list.length > 0)
			{
				sb.append(list[0]);
				
				for(int i = 1; i < list.length; i++)
				{
					sb.append(",");
					sb.append(list[i]);
				}
			}
			
			try
			{
				try
				{
					m_db.beginTransaction(SqlJetTransactionMode.WRITE);

					m_paramTable.insert(key, sb.toString());

					m_db.commit();
				}
				catch(Exception e)
				{
					m_db.beginTransaction(SqlJetTransactionMode.WRITE);

					ISqlJetCursor updateCursor =  m_paramTable.open();

					updateCursor.update(key, sb.toString());

					updateCursor.close();
					
					m_db.commit();
				}	
			}	
			catch(Exception e)
			{

			}
		}
	}
	
	public SqlJetDb getDatabase()
	{
		return m_db;
	}

	public int getParameterInt(String key, int defaultValue)
	{
		String str = getParameter(key);
		
		try
		{
			return Integer.decode(str);
		}
		catch(Exception e)
		{
		}
		
		return defaultValue;
	}
	
	
	public boolean getParameterBool(String key, boolean defaultValue)
	{
		String str = getParameter(key);
		
		try
		{
			return Boolean.getBoolean(str);
		}
		catch(Exception e)
		{
		}
		
		return defaultValue;
	}
	
	
	public float getParameterFloat(String key, float defaultValue)
	{
		String str = getParameter(key);
		
		try
		{
			return Float.valueOf(str);
		}
		catch(Exception e)
		{
		}
		
		return defaultValue;
	}
}
