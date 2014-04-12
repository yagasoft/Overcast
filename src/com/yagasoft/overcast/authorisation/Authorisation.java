/* 
 * Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
 * 
 *		Modified MIT License (GPL v3 compatible)
 * 			License terms are in a separate file (license.txt)
 * 
 *		Project/File: Overcast/com.yagasoft.overcast.authorisation/Authorisation.java
 * 
 *			Modified: 11-Apr-2014 (12:34:32)
 *			   Using: Eclipse J-EE / JDK 7 / Windows 8.1 x64
 */

package com.yagasoft.overcast.authorisation;


import com.yagasoft.overcast.exception.AuthorisationException;


/**
 * The Class Authorisation.
 */
public abstract class Authorisation
{
	
	/** User ID, which can be an identifier for the account. */
	protected String	userID;
	
	/** Password (if needed; most use OAuth anyway). */
	protected String	password;
	
	/**
	 * Go through the authorisation process.<br />
	 * Might get a token using OAuth, or use a user and pass to access FTP, ... etc.<br />
	 * In case of OAuth, it should check if a token already exists/valid, and if not, then fetch a new one.
	 * 
	 * @throws AuthorisationException
	 *             the authorisation exception
	 */
	public abstract void authorise() throws AuthorisationException;
	
	/**
	 * Instantiates a new authorisation.
	 */
	public Authorisation()
	{}
	
	/**
	 * Instantiates a new authorisation.
	 * 
	 * @param userID
	 *            User id.
	 * @param password
	 *            Password.
	 */
	public Authorisation(String userID, String password)
	{
		this.userID = userID;
		this.password = password;
	}
	
	/**
	 * Gets the user id.
	 * 
	 * @return the user id
	 */
	public String getUserID()
	{
		return userID;
	}
	
	/**
	 * Sets the user id.
	 * 
	 * @param value
	 *            the new user id
	 */
	public void setUserID(String value)
	{
		userID = value;
	}
	
	/**
	 * Gets the password.
	 * 
	 * @return the password
	 */
	public String getPassword()
	{
		return password;
	}
	
	/**
	 * Sets the password.
	 * 
	 * @param password
	 *            the password to set
	 */
	public void setPassword(String password)
	{
		this.password = password;
	}
}
