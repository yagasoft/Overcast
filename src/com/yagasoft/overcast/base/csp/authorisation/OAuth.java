/* 
 * Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
 * 
 *		Modified MIT License (GPL v3 compatible)
 * 			License terms are in a separate file (license.txt)
 * 
 *		Project/File: Overcast/com.yagasoft.overcast.authorisation/OAuth.java
 * 
 *			Modified: 11-Apr-2014 (12:31:01)
 *			   Using: Eclipse J-EE / JDK 7 / Windows 8.1 x64
 */

package com.yagasoft.overcast.base.csp.authorisation;


import java.nio.file.Path;

import com.yagasoft.overcast.exception.AuthorisationException;


/**
 * The Class OAuth.
 */
public abstract class OAuth extends Authorisation
{
	
	/** Parent folder to store tokens. */
	protected Path	parent;
	
	/** Info file containing info to get token. */
	protected Path	infoFile;
	
	/** Token value (if needed; usually saved to disk in JSON format). */
	protected int	token;
	
	/** Refresh token value. */
	protected int	refreshToken;
	
	/**
	 * Instantiates a new o auth.
	 */
	public OAuth()
	{
		super();
	}
	
	/**
	 * Instantiates a new OAuth.
	 * 
	 * @param userID
	 *            User id.
	 * @param password
	 *            Password.
	 */
	public OAuth(String userID, String password)
	{
		super(userID, password);
	}
	
	/**
	 * Instantiates a new OAuth.
	 * 
	 * @param parent
	 *            Parent.
	 * @param infoFile
	 *            TODO
	 */
	public OAuth(Path parent, Path infoFile)
	{
		this.parent = parent;
		this.infoFile = infoFile;
	}
	
	/**
	 * Instantiates a new OAuth.
	 * 
	 * @param userID
	 *            User id.
	 * @param password
	 *            Password.
	 * @param parent
	 *            Parent.
	 * @param infoFile
	 *            TODO
	 */
	public OAuth(String userID, String password, Path parent, Path infoFile)
	{
		super(userID, password);
		this.parent = parent;
		this.infoFile = infoFile;
	}
	
	/**
	 * Acquire permission from scratch.<br />
	 * Might open the browser to get user's permission, then use the code returned to get a token.
	 * 
	 * @throws AuthorisationException
	 *             the authorisation exception
	 */
	public abstract void acquirePermission() throws AuthorisationException;
	
	/**
	 * Re-acquire permission using a saved token or code.
	 * 
	 * @throws AuthorisationException
	 *             the authorisation exception
	 */
	public abstract void reacquirePermission() throws AuthorisationException;
	
	/**
	 * Save token received to disk or field.
	 * 
	 * @throws AuthorisationException
	 *             the authorisation exception
	 */
	protected abstract void saveToken() throws AuthorisationException;
	
	// //////////////////////////////////////////////////////////////////////////////////////
	// #region Getters and setters.
	// ======================================================================================
	
	/**
	 * Gets the parent.
	 * 
	 * @return the parent
	 */
	public Path getParent()
	{
		return parent;
	}
	
	/**
	 * Sets the parent.
	 * 
	 * @param parent
	 *            the parent to set
	 */
	public void setParent(Path parent)
	{
		this.parent = parent;
	}
	
	/**
	 * Gets the info file.
	 * 
	 * @return the info file
	 */
	public java.nio.file.Path getInfoFile()
	{
		return infoFile;
	}
	
	/**
	 * Sets the info file.
	 * 
	 * @param value
	 *            the new info file
	 */
	public void setInfoFile(Path value)
	{
		infoFile = value;
	}
	
	/**
	 * Gets the token.
	 * 
	 * @return the token
	 */
	public int getToken()
	{
		return token;
	}
	
	/**
	 * Sets the token.
	 * 
	 * @param value
	 *            the new token
	 */
	public void setToken(int value)
	{
		token = value;
	}
	
	/**
	 * Gets the refresh token.
	 * 
	 * @return the refresh token
	 */
	public int getRefreshToken()
	{
		return refreshToken;
	}
	
	/**
	 * Sets the refresh token.
	 * 
	 * @param value
	 *            the new refresh token
	 */
	public void setRefreshToken(int value)
	{
		refreshToken = value;
	}
	
	// ======================================================================================
	// #endregion Getters and setters.
	// //////////////////////////////////////////////////////////////////////////////////////
	
}
