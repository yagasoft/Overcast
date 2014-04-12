/*
 * Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
 *
 *		Modified MIT License (GPL v3 compatible)
 * 			License terms are in a separate file (license.txt)
 *
 *		Project/File: Overcast/com.yagasoft.overcast.dropbox/Authorisation.java
 *
 *			Modified: 11-Apr-2014 (12:54:08)
 *			   Using: Eclipse J-EE / JDK 7 / Windows 8.1 x64
 */

package com.yagasoft.overcast.dropbox.authorisation;


import java.awt.Desktop;
import java.awt.Desktop.Action;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;

import com.dropbox.core.DbxAppInfo;
import com.dropbox.core.DbxAuthFinish;
import com.dropbox.core.DbxAuthInfo;
import com.dropbox.core.DbxClient;
import com.dropbox.core.DbxException;
import com.dropbox.core.json.JsonReader.FileLoadException;
import com.yagasoft.overcast.authorisation.OAuth;
import com.yagasoft.overcast.dropbox.Dropbox;
import com.yagasoft.overcast.exception.AuthorisationException;


/**
 * A class to handle OAuth operations with Dropbox.<br />
 * <br />
 * Make sure to add a redirect URI to your dev account.<br />
 * The URI should be in the form "http://[host]:[port]/callback".<br />
 */
public class Authorisation extends OAuth
{

	/** Receiver. */
	private LocalServerReceiver	receiver;

	/** Redirect URI. */
	private String				redirectUri;

	/** Auth finishing object. */
	private DbxAuthFinish		authFinish;

	/** App info. */
	private DbxAppInfo			appInfo;

	/** Auth info. */
	protected DbxAuthInfo		authInfo;


	/**
	 * Instantiates a new authorisation.
	 *
	 * @param infoFile Info file.
	 * @param port Port for listening server.
	 * @throws AuthorisationException the authorisation exception
	 */
	public Authorisation(Path infoFile, int port) throws AuthorisationException
	{
		this("user", infoFile, port);
	}

	/**
	 * @param userID
	 * @param password
	 * @param info
	 * @throws AuthorisationException
	 */
	public Authorisation(String userID, Path infoFile, int port) throws AuthorisationException
	{
		super(infoFile.getParent(), infoFile);
		this.userID = userID;
		setupServer(port);
	}

	/**
	 * Sets the up server to receive authorisation code.
	 *
	 * @param port any free port on localhost.
	 */
	public void setupServer(int port)
	{
		receiver = new LocalServerReceiver("localhost", port);
		redirectUri = receiver.getUri();
	}

	/**
	 * @see com.yagasoft.overcast.authorisation.Authorisation#authorise()
	 */
	@Override
	public void authorise() throws AuthorisationException
	{
		try
		{
			// read saved token and check validity.
			reacquirePermission();
		}
		catch (AuthorisationException e)
		{	// token invalid or missing.
			acquirePermission();		// open browser to ask for user permission, then save token.
		}
	}

	/**
	 * @see com.yagasoft.overcast.authorisation.OAuth#acquirePermission()
	 */
	@Override
	public void acquirePermission() throws AuthorisationException
	{
		try
		{
			// Read app info file (contains app key and app secret)
			appInfo = DbxAppInfo.Reader.readFromFile(infoFile.toString());

			// Run through Dropbox API authorization process
			DbxWebAuthNoRedirect webAuth = new DbxWebAuthNoRedirect(Dropbox.getRequestConfig(), appInfo, redirectUri);
			String authoriseUrl = webAuth.start();		// get URL to send to browser
			String code = authorise(authoriseUrl, redirectUri);		// opens browser and get access code.

			// problem with getting code from browser?
			if (code == null)
			{
				throw new AuthorisationException("Failed to authorise!");
			}

			code = code.trim();
			authFinish = webAuth.finish(code);		// get access token using access code.

			saveToken();
		}
		catch (FileLoadException | DbxException e)
		{
			e.printStackTrace();
			throw new AuthorisationException("Failed to authorise! " + e.getMessage());
		}

	}

	/**
	 * @see com.yagasoft.overcast.authorisation.OAuth#reacquirePermission()
	 */
	@Override
	public void reacquirePermission() throws AuthorisationException
	{
		try
		{
			// read the token from disk.
			authInfo = DbxAuthInfo.Reader.readFromFile(new File(parent.toFile(), "dropbox_token.dat"));

			// make sure the token is valid.
			DbxClient dbxClient = new DbxClient(Dropbox.getRequestConfig(), authInfo.accessToken, authInfo.host);
			dbxClient.getAccountInfo();
		}
		catch (FileLoadException | DbxException e)
		{
			throw new AuthorisationException("Failed to authorise! " + e.getMessage());
		}
	}

	/**
	 * @see com.yagasoft.overcast.authorisation.OAuth#saveToken()
	 */
	@Override
	protected void saveToken() throws AuthorisationException
	{
		try
		{
			// prepare access token to be saved to disk.
			authInfo = new DbxAuthInfo(authFinish.accessToken, appInfo.host);

			// Save auth information to output file.
			DbxAuthInfo.Writer.writeToFile(authInfo, new File(parent.toFile(), "dropbox_token.dat"));
		}
		catch (IOException e)
		{
			e.printStackTrace();
			throw new AuthorisationException("Failed to authorise! " + e.getMessage());
		}
	}

	/**
	 * Authorises the installed application to access user's protected data.
	 *
	 * @param url
	 *            Url.
	 * @param redirectUri
	 *            Redirect uri.
	 * @return credential
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws AuthorisationException
	 *             the authorisation exception
	 */
	private String authorise(String url, String redirectUri) throws AuthorisationException
	{
		// server parameters set?
		if (receiver == null)
		{
			throw new AuthorisationException("Failed to authorise! Server not setup.");
		}

		try
		{
			// open browser to get access code.
			browse(url + "&redirect_uri=" + receiver.getRedirectUri());
	
			// receive authorisation code.
			String code = receiver.waitForCode();
	
			// stop the listening server.
			receiver.stop();

			return code;
		}
		catch (IOException e)
		{
			e.printStackTrace();
			throw new AuthorisationException("Failed to authorise! " + e.getMessage());
		}
	}

	/**
	 * Open a browser at the given URL using {@link Desktop} if available, or alternatively output the
	 * URL to {@link System#out} for command-line applications.
	 *
	 * @param url
	 *            URL to browse
	 * @throws AuthorisationException 
	 */
	private void browse(String url) throws AuthorisationException
	{
		// Ask user to open in their browser using copy-paste
		System.out.println("Please open the following address in your browser:");
		System.out.println("  " + url);

		// Attempt to open it in the browser
		try
		{
			if (Desktop.isDesktopSupported())
			{
				Desktop desktop = Desktop.getDesktop();
				if (desktop.isSupported(Action.BROWSE))
				{
					System.out.println("Attempting to open that address in the default browser now...");
					desktop.browse(URI.create(url));
				}
			}
		}
		catch (IOException | InternalError e)
		{
			e.printStackTrace();
			// A bug in a JRE can cause Desktop.isDesktopSupported() to throw an
			// InternalError rather than returning false. The error reads,
			// "Can't connect to X11 window server using ':0.0' as the value of the
			// DISPLAY variable." The exact error message may vary slightly.
			throw new AuthorisationException("Failed to authorise! " + e.getMessage());
		}
	}

	/**
	 * @return the appInfo
	 */
	public DbxAppInfo getAppInfo()
	{
		return appInfo;
	}


	/**
	 * @param appInfo the appInfo to set
	 */
	public void setAppInfo(DbxAppInfo appInfo)
	{
		this.appInfo = appInfo;
	}


	/**
	 * @return the authInfo
	 */
	public DbxAuthInfo getAuthInfo()
	{
		return authInfo;
	}


	/**
	 * @param authInfo the authInfo to set
	 */
	public void setAuthInfo(DbxAuthInfo authInfo)
	{
		this.authInfo = authInfo;
	}

}
