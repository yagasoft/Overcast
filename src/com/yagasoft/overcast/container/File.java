/* 
 * Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
 * 
 *		Modified MIT License (GPL v3 compatible)
 * 			License terms are in a separate file (license.txt)
 *
 */

package com.yagasoft.overcast.container;


/**
 * Class representing the commonalities between all file types (remote/local).
 *
 * @param <T> the type of the file in the original API of the CSP.
 */
public abstract class File<T> extends Container<T>
{
	
	/**
	 * Type of the file. It's better to generate this type in {@link File#updateInfo()} based on the extension
	 * , or MIME from the CSP.
	 */
	protected String	type;
	
	/**
	 * @see com.yagasoft.overcast.container.Container#isFolder()
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
