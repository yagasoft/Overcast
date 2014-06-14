/*
 * Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
 * 
 *		The Modified MIT Licence (GPL v3 compatible)
 * 			License terms are in a separate file (LICENCE.md)
 * 
 *		Project/File: Overcast/com.yagasoft.overcast.base.container/File.java
 * 
 *			Modified: Apr 14, 2014 (2:27:29 PM)
 *			   Using: Eclipse J-EE / JDK 7 / Windows 8.1 x64
 */

package com.yagasoft.overcast.base.container;


/**
 * Class representing the commonalities between all file types (remote/local).
 * 
 * @param <T>
 *            the type of the file in the original API of the CSP.
 */
public abstract class File<T> extends Container<T>
{
	
	/**
	 * Type of the file. It's better to generate this type in {@link File#updateInfo()} based on the extension
	 * , or MIME from the CSP.
	 */
	protected String	type;
	
	/**
	 * @see com.yagasoft.overcast.base.container.Container#isFolder()
	 */
	@Override
	public boolean isFolder()
	{
		return false;
	}
	
	/**
	 * @return the type
	 */
	public String getType()
	{
		return type;
	}
	
	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(String type)
	{
		this.type = type;
	}
	
}
