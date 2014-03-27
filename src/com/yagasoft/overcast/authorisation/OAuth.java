/* 
 * Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
 * 
 *		Modified MIT License (GPL v3 compatible)
 * 			License terms are in a separate file (license.txt)
 * 
 *		Project/File: Overcast/com.yagasoft.overcast.authorisation/OAuth.java
 * 
 *			Modified: 27-Mar-2014 (16:11:53)
 *			   Using: Eclipse J-EE / JDK 7 / Windows 8.1 x64
 */

package com.yagasoft.overcast.authorisation;


import java.nio.file.Path;

import com.yagasoft.overcast.exception.AuthorisationException;


public abstract class OAuth extends Authorisation
{
	
	protected Path	parent;
	protected Path	infoFile;
	protected int	token;
	protected int	refreshToken;
	
	public abstract void acquirePermission() throws AuthorisationException;
	
	public abstract void reacquirePermission() throws AuthorisationException;
	
	public abstract void saveToken();
	
	/**
	 *
	 */
	public OAuth()
	{
		super();
	}
	
	/**
	 * @param userID
	 * @param password
	 */
	public OAuth(String userID, String password)
	{
		super(userID, password);
	}
	
	/**
	 * @param parent
	 * @param infoFile
	 *            TODO
	 */
	public OAuth(Path parent, Path infoFile)
	{
		this.parent = parent;
		this.infoFile = infoFile;
	}
	
	/**
	 * @param userID
	 * @param password
	 * @param parent
	 * @param infoFile
	 *            TODO
	 */
	public OAuth(String userID, String password, Path parent, Path infoFile)
	{
		super(userID, password);
		this.parent = parent;
		this.infoFile = infoFile;
	}
	
	// //////////////////////////////////////////////////////////////////////////////////////
	// #region Getters and setters.
	// ======================================================================================
	
	/**
	 * @return the parent
	 */
	public Path getParent()
	{
		return parent;
	}
	
	/**
	 * @param parent
	 *            the parent to set
	 */
	public void setParent(Path parent)
	{
		this.parent = parent;
	}
	
	public java.nio.file.Path getInfoFile()
	{
		return infoFile;
	}
	
	public void setInfoFile(Path value)
	{
		infoFile = value;
	}
	
	public int getToken()
	{
		return token;
	}
	
	public void setToken(int value)
	{
		token = value;
	}
	
	public int getRefreshToken()
	{
		return refreshToken;
	}
	
	public void setRefreshToken(int value)
	{
		refreshToken = value;
	}
	
	// ======================================================================================
	// #endregion Getters and setters.
	// //////////////////////////////////////////////////////////////////////////////////////
	
}
