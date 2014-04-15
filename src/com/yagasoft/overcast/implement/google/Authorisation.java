/* 
 * Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
 * 
 *		The Modified MIT Licence (GPL v3 compatible)
 * 			License terms are in a separate file (LICENCE.md)
 * 
 *		Project/File: Overcast/com.yagasoft.overcast.implement.google/Authorisation.java
 * 
 *			Modified: Apr 15, 2014 (1:51:15 PM)
 *			   Using: Eclipse J-EE / JDK 7 / Windows 8.1 x64
 */

package com.yagasoft.overcast.implement.google;


import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.util.store.DataStoreFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.DriveScopes;
import com.yagasoft.logger.Logger;
import com.yagasoft.overcast.base.csp.authorisation.OAuth;
import com.yagasoft.overcast.exception.AuthorisationException;


/**
 * A class to handle OAuth operations with Google.
 */
public class Authorisation extends OAuth
{

	/** Directory to store user credentials. */
	protected Path							dataStoreFolder;

	/**
	 * Global instance of the {@link DataStoreFactory}.
	 */
	protected FileDataStoreFactory			dataStoreFactory;

	/** The credential. */
	protected Credential					credential;

	/** The flow. */
	protected GoogleAuthorizationCodeFlow	flow;

	/**
	 * @param info
	 */
	public Authorisation(String infoFile) throws AuthorisationException
	{
		super("user", infoFile);
	}

	/**
	 * @param userID
	 * @param password
	 * @param info
	 * @throws AuthorisationException
	 */
	public Authorisation(String userID, String infoFile) throws AuthorisationException
	{
		super(userID, infoFile);
	}

	/**
	 * @see com.yagasoft.overcast.base.csp.authorisation.Authorisation#authorise()
	 */
	@Override
	public void authorise() throws AuthorisationException
	{
		// go online and get the token.
		acquirePermission();
	}

	/**
	 * @see com.yagasoft.overcast.base.csp.authorisation.OAuth#acquirePermission()
	 */
	@Override
	public void acquirePermission() throws AuthorisationException
	{
		Logger.newEntry("authorising ...");
		
		// authorise
		try
		{
			// the folder where Google API stores creds.
			dataStoreFolder = tokenParent;
			dataStoreFactory = new FileDataStoreFactory(dataStoreFolder.toFile());

			// load the JSON containing info required for identifying the dev account.
			GoogleClientSecrets clientSecrets =
					GoogleClientSecrets.load(Google.JSON_FACTORY,
							new InputStreamReader(Files.newInputStream(infoFile)));

			// dev info contained in the JSON.
			String clientId = clientSecrets.getDetails().getClientId();
			String clientSecret = clientSecrets.getDetails().getClientSecret();

			// problem?!
			if (clientId.startsWith("Enter") || clientSecret.startsWith("Enter"))
			{
				Logger.newEntry("Enter Client ID and Secret from https://code.google.com/apis/console/?api=drive "
						+ "into google_secrets.json");
				
				throw new AuthorisationException("Failed to authorise!");
			}

			// set up authorisation code flow
			flow = new GoogleAuthorizationCodeFlow.Builder(Google.httpTransport, Google.JSON_FACTORY, clientSecrets,
					Collections.singleton(DriveScopes.DRIVE))
					.setDataStoreFactory(dataStoreFactory).setAccessType("offline").build();

			credential = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize(userID);
			
			Logger.newEntry("authorisation successful");
		}
		catch (IOException e)
		{
			Logger.newEntry("authorisation failed!");
			
			e.printStackTrace();
			throw new AuthorisationException("Failed to authorise! " + e.getMessage());
		}

	}

	/**
	 * @see com.yagasoft.overcast.base.csp.authorisation.OAuth#reacquirePermission()
	 */
	@Override
	public void reacquirePermission() throws AuthorisationException
	{
		acquirePermission();
	}

	/**
	 * @see com.yagasoft.overcast.base.csp.authorisation.OAuth#saveToken()
	 */
	@Override
	protected void saveToken() throws AuthorisationException
	{
		throw new UnsupportedOperationException("Google handles saving tokens automatically!");
	}

	/**
	 * @return the credential
	 */
	public Credential getCredential()
	{
		return credential;
	}

	/**
	 * @param credential
	 *            the credential to set
	 */
	public void setCredential(Credential credential)
	{
		this.credential = credential;
	}

}
