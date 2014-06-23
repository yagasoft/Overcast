/*
 * Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
 *
 *		The Modified MIT Licence (GPL v3 compatible)
 * 			Licence terms are in a separate file (LICENCE.md)
 *
 *		Project/File: Overcast/com.yagasoft.overcast.base.csp.authorisation/OAuth.java
 *
 *			Modified: 24-Jun-2014 (01:07:36)
 *			   Using: Eclipse J-EE / JDK 8 / Windows 8.1 x64
 */

package com.yagasoft.overcast.base.csp.authorisation;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.yagasoft.logger.Logger;
import com.yagasoft.overcast.exception.AuthorisationException;


/**
 * This class is used to get authorisation to access a CSP that uses the OAuth standard.
 */
public abstract class OAuth extends Authorisation
{

	/** Info file parent. */
	private Path	infoParent	= Paths.get(System.getProperty("user.dir") + "/etc/secrets");

	/** Parent folder to store tokens. */
	protected Path	tokenParent	= Paths.get(System.getProperty("user.dir") + "/var/tokens");

	/** Info file path. This file should contain the information required to identify the dev's account at the CSP. */
	protected Path	infoFile;

	/** Token value, if needed; usually saved to disk in JSON format). */
	protected int	token;

	/** Refresh token value. */
	protected int	refreshToken;

	/**
	 * Instantiates a new OAuth.
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
	 * @param infoFile
	 *            the info file path as a string, will be used to create its path object.
	 * @throws AuthorisationException
	 */
	public OAuth(String infoFile) throws AuthorisationException
	{
		this(null, null, infoFile);
	}

	/**
	 * Instantiates a new OAuth.
	 *
	 * @param userID
	 *            User id.
	 * @param password
	 *            Password.
	 * @param infoFile
	 *            the info file path as a string, will be used to create its path object.
	 * @throws AuthorisationException
	 */
	public OAuth(String userID, String password, String infoFile) throws AuthorisationException
	{
		this(userID, password);

		try
		{
			Files.createDirectories(infoParent);
			Files.createDirectories(tokenParent);
		}
		catch (IOException e)
		{
			Logger.error("OVERCAST: OAuth: creating authorisation object -- creating folders: " + infoFile);
			Logger.except(e);
			e.printStackTrace();

			throw new AuthorisationException(e.getMessage());
		}

		this.infoFile = infoParent.resolve(infoFile);		// create a path object for the info file.
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
	 * Re-acquire permission using a saved code or refresh token.
	 *
	 * @throws AuthorisationException
	 *             the authorisation exception
	 */
	public abstract void reacquirePermission() throws AuthorisationException;

	/**
	 * Reset token and try to acquire permission.
	 *
	 * @throws AuthorisationException
	 *             the authorisation exception
	 */
	public abstract void resetPermission() throws AuthorisationException;

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
	 * Gets the token parent.
	 *
	 * @return the parent
	 */
	public Path getTokenParent()
	{
		return tokenParent;
	}

	/**
	 * Sets the token parent.
	 *
	 * @param parent
	 *            the parent to set
	 */
	public void setTokenParent(Path parent)
	{
		tokenParent = parent;
	}

	/**
	 * Gets the info file.
	 *
	 * @return the info file
	 */
	public Path getInfoFile()
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

	/**
	 * @return the infoParent
	 */
	public Path getInfoParent()
	{
		return infoParent;
	}

	/**
	 * @param infoParent
	 *            the infoParent to set
	 */
	public void setInfoParent(Path infoParent)
	{
		this.infoParent = infoParent;
		infoFile = infoParent.resolve(infoFile);
	}

	// ======================================================================================
	// #endregion Getters and setters.
	// //////////////////////////////////////////////////////////////////////////////////////

}
