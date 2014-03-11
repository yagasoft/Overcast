/*
 * Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
 *
 *		Modified MIT License (GPL v3 compatible)
 * 			License terms are in a separate file (license.txt)
 *
 */

package com.yagasoft.overcast.google;


import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
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
	 * @see com.yagasoft.overcast.authorisation.Authorisation#authorise()
	 */
	@Override
	public void authorise()
	{
		try
		{
			// the folder where Google API stores creds.
			dataStoreFolder = Paths.get(Authorisation.class.getResource("/").toURI());
			dataStoreFactory = new FileDataStoreFactory(dataStoreFolder.toFile());
			
			// load the JSON containing info required for identifying the dev account.
			GoogleClientSecrets clientSecrets =
					GoogleClientSecrets.load(Google.JSON_FACTORY,
							new InputStreamReader(Authorisation.class.getResourceAsStream("/" + info.getFileName())));
			
			// dev info contained in the JSON.
			String clientId = clientSecrets.getDetails().getClientId();
			String clientSecret = clientSecrets.getDetails().getClientSecret();
			
			// problem!
			if (clientId.startsWith("Enter") || clientSecret.startsWith("Enter "))
			{
				Logger.post("Enter Client ID and Secret from https://code.google.com/apis/console/?api=drive "
								+ "into ./client_secrets.json");
				System.exit(1);
			}
			
			// set up authorisation code flow
			flow = new GoogleAuthorizationCodeFlow.Builder(Google.httpTransport, Google.JSON_FACTORY, clientSecrets,
					Collections.singleton(DriveScopes.DRIVE))
					.setDataStoreFactory(dataStoreFactory).build();
			
			// go online and get the token.
			acquirePermission();
		}
		catch (IOException | URISyntaxException e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * @see com.yagasoft.overcast.authorisation.OAuth#acquirePermission()
	 */
	@Override
	public void acquirePermission()
	{
		// authorise
		try
		{
			credential = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
	}
	
	/**
	 * @see com.yagasoft.overcast.authorisation.OAuth#reacquirePermission()
	 */
	@Override
	public void reacquirePermission()
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
