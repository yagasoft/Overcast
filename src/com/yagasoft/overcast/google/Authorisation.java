/* 
 * Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
 * 
 *		Modified MIT License (GPL v3 compatible)
 * 			License terms are in a separate file (license.txt)
 * 
 *		Project/File: Overcast/com.yagasoft.overcast.google/Authorisation.java
 * 
 *			Modified: 27-Mar-2014 (16:14:42)
 *			   Using: Eclipse J-EE / JDK 7 / Windows 8.1 x64
 */

package com.yagasoft.overcast.google;


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
import com.yagasoft.overcast.authorisation.OAuth;
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
	public Authorisation(Path infoFile) throws AuthorisationException
	{
		super(infoFile.getParent(), infoFile);
		userID = "user";
	}
	
	/**
	 * @param userID
	 * @param password
	 * @param info
	 * @throws AuthorisationException
	 */
	public Authorisation(String userID, Path infoFile) throws AuthorisationException
	{
		super(infoFile.getParent(), infoFile);
		this.userID = userID;
	}
	
	/**
	 * @see com.yagasoft.overcast.authorisation.Authorisation#authorise()
	 */
	@Override
	public void authorise() throws AuthorisationException
	{
		try
		{
			// the folder where Google API stores creds.
			dataStoreFolder = parent;
			dataStoreFactory = new FileDataStoreFactory(dataStoreFolder.toFile());
			
			// load the JSON containing info required for identifying the dev account.
			GoogleClientSecrets clientSecrets =
					GoogleClientSecrets.load(Google.JSON_FACTORY,
							new InputStreamReader(Files.newInputStream(infoFile)));
			
			// dev info contained in the JSON.
			String clientId = clientSecrets.getDetails().getClientId();
			String clientSecret = clientSecrets.getDetails().getClientSecret();
			
			// problem!
			if (clientId.startsWith("Enter") || clientSecret.startsWith("Enter "))
			{
				Logger.post("Enter Client ID and Secret from https://code.google.com/apis/console/?api=drive "
						+ "into ./client_secrets.json");
				throw new AuthorisationException("Failed to authorise!");
			}
			
			// set up authorisation code flow
			flow = new GoogleAuthorizationCodeFlow.Builder(Google.httpTransport, Google.JSON_FACTORY, clientSecrets,
					Collections.singleton(DriveScopes.DRIVE))
					.setDataStoreFactory(dataStoreFactory).setAccessType("offline").build();
			
			// go online and get the token.
			acquirePermission();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			throw new AuthorisationException("Failed to authorise!");
		}
	}
	
	/**
	 * @see com.yagasoft.overcast.authorisation.OAuth#acquirePermission()
	 */
	@Override
	public void acquirePermission() throws AuthorisationException
	{
		// authorise
		try
		{
			credential = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize(userID);
		}
		catch (IOException e)
		{
			e.printStackTrace();
			throw new AuthorisationException("Failed to authorise!");
		}
		
	}
	
	/**
	 * @see com.yagasoft.overcast.authorisation.OAuth#reacquirePermission()
	 */
	@Override
	public void reacquirePermission() throws AuthorisationException
	{
		acquirePermission();
	}
	
	/**
	 * @see com.yagasoft.overcast.authorisation.OAuth#saveToken()
	 */
	@Override
	public void saveToken()
	{	
		
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
