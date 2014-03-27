/* 
 * Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
 * 
 *		Modified MIT License (GPL v3 compatible)
 * 			License terms are in a separate file (license.txt)
 * 
 *		Project/File: Overcast/com.yagasoft.overcast.authorisation/Authorisation.java
 * 
 *			Modified: 27-Mar-2014 (16:11:48)
 *			   Using: Eclipse J-EE / JDK 7 / Windows 8.1 x64
 */

package com.yagasoft.overcast.authorisation;


import com.yagasoft.overcast.exception.AuthorisationException;


public abstract class Authorisation
{
	
	protected String	userID;
	protected String	password;
	
	public abstract void authorise() throws AuthorisationException;
	
	public Authorisation()
	{}
	
	/**
	 * @param userID
	 * @param password
	 */
	public Authorisation(String userID, String password)
	{
		super();
		this.userID = userID;
		this.password = password;
	}
	
	public String getUserID()
	{
		return userID;
	}
	
	public void setUserID(String value)
	{
		userID = value;
	}
	
	/**
	 * @return the password
	 */
	public String getPassword()
	{
		return password;
	}
	
	/**
	 * @param password
	 *            the password to set
	 */
	public void setPassword(String password)
	{
		this.password = password;
	}
}
