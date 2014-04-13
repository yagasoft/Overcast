/*
 * Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
 *
 *		Modified MIT License (GPL v3 compatible)
 * 			License terms are in a separate file (license.txt)
 *
 *		Project/File: Overcast/com.yagasoft.overcast.ubuntu/Authorisation.java
 *
 *			Modified: 27-Mar-2014 (16:15:19)
 *			   Using: Eclipse J-EE / JDK 7 / Windows 8.1 x64
 */

package com.yagasoft.overcast.implement.ubuntu;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import oauth.signpost.signature.HmacSha1MessageSigner;
import oauth.signpost.signature.PlainTextMessageSigner;

import com.ubuntuone.api.sso.U1AuthAPI;
import com.ubuntuone.api.sso.authorizer.BasicAuthorizer;
import com.ubuntuone.api.sso.authorizer.OAuthAuthorizer;
import com.ubuntuone.api.sso.exceptions.AuthenticationException;
import com.ubuntuone.api.sso.exceptions.TimeDriftException;
import com.ubuntuone.api.sso.exceptions.U1PingException;
import com.ubuntuone.api.sso.model.AuthenticateResponse;
import com.yagasoft.overcast.base.csp.authorisation.OAuth;
import com.yagasoft.overcast.exception.AuthorisationException;


public class Authorisation extends OAuth
{
	
	private File			authFile;
	private String			oauthData;
	private OAuthAuthorizer	oauthAuthorizer;
	
	/**
	 * @param userID
	 * @param password
	 * @param parent
	 */
	public Authorisation(String userID, String password)
	{
		super(userID, password);
	}
	
	@Override
	public void authorise() throws AuthorisationException
	{
		
		try
		{
			// See if we already have stored an OAuth token.
			authFile = new File(tokenParent.toString(), "ubuntu_token.dat");
			
			if (authFile.exists())
			{
				final Properties creds = new Properties();
				creds.load(new FileInputStream(authFile));
				oauthData = creds.getProperty("oauth");
			}
			else
			{
				acquirePermission();
				saveToken();
			}
			
			// OAuth data we received contains consumerKey, consumerSecret, tokenKey and tokenSecret.
			setOauthAuthorizer(OAuthAuthorizer.getWithTokens(oauthData, new PlainTextMessageSigner()));
			
			// Make sure our time is not off too much.
			OAuthAuthorizer.syncTimeWithU1(Ubuntu.httpClient);
		}
		catch (TimeDriftException e)
		{
			e.printStackTrace();
			System.err.println("Could not sync time with Ubuntu One. Sync with NTP?");
			throw new AuthorisationException("Failed to authorise!");
		}
		catch (IOException e)
		{
			e.printStackTrace();
			throw new AuthorisationException("Failed to authorise!");
		}
		
	}
	
	@Override
	public void acquirePermission() throws AuthorisationException
	{
		try
		{
			// We need to generate an access token at login.ubuntu.com and transfer it to Ubuntu One, in order to successfully
			// communicate with Ubuntu One.
			AuthenticateResponse auth = authenticate();
			oauthData = auth.getSerialized();
		}
		catch (AuthenticationException e)
		{
			e.printStackTrace();
			System.err.println("Wrong username or password.");
			throw new AuthorisationException("Failed to authorise!");
		}
		catch (IOException e)
		{
			e.printStackTrace();
			System.err.println("Connection problem: " + e.getMessage());
			throw new AuthorisationException("Failed to authorise!");
		}
		catch (TimeDriftException e)
		{
			e.printStackTrace();
			System.err.println("Could not sync time with Ubuntu One. Sync with NTP?");
			throw new AuthorisationException("Failed to authorise!");
		}
		catch (U1PingException e)
		{
			e.printStackTrace();
			System.err.println("Could not authorize access to Ubuntu One. Please try again.");
			throw new AuthorisationException("Failed to authorise!");
		}
	}
	
	private AuthenticateResponse authenticate() throws AuthenticationException, IOException, TimeDriftException, U1PingException
	{
		final BasicAuthorizer basicAuthorizer = new BasicAuthorizer(userID, password);
		
		final U1AuthAPI authApi = new U1AuthAPI(Ubuntu.class.getPackage().getName(), "1.0", Ubuntu.httpClient, basicAuthorizer);
		
		// Make sure your token name is in the format of 'Ubuntu One @ your_string"
		final AuthenticateResponse response = authApi.authenticate("Ubuntu One @ Overcast");
		authApi.setAuthorizer(new OAuthAuthorizer(response, new HmacSha1MessageSigner()));
		
		// Make sure our time is not off too much.
		OAuthAuthorizer.syncTimeWithU1(Ubuntu.httpClient);
		
		// Single Sign On has the token, let Ubuntu One know about it.
		authApi.pingUbuntuOne(userID);
		
		return response;
	}
	
	@Override
	public void reacquirePermission() throws AuthorisationException
	{}
	
	@Override
	public void saveToken()
	{
		
		// Save the token for later reuse. Usually a safer place than a plain text file is strongly advised.
		final Properties creds = new Properties();
		
		try
		{
			creds.put("oauth", oauthData);
			creds.store(new FileOutputStream(authFile), "");
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
	}
	
	/**
	 * @return the oauthAuthorizer
	 */
	public OAuthAuthorizer getOauthAuthorizer()
	{
		return oauthAuthorizer;
	}
	
	/**
	 * @param oauthAuthorizer
	 *            the oauthAuthorizer to set
	 */
	public void setOauthAuthorizer(OAuthAuthorizer oauthAuthorizer)
	{
		this.oauthAuthorizer = oauthAuthorizer;
	}
	
}
