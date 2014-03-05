
package com.yagasoft.overcast.ubuntu;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
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
import com.yagasoft.overcast.authorisation.OAuth;


public class Authorisation extends OAuth
{
	
	private String	username;
	private String	password;
	private File	info;
	private String	oauthData;
	OAuthAuthorizer	oauthAuthorizer;
	
	
	public Authorisation(String username, String password)
	{
		this.username = username;
		this.password = password;
	}
	
	@Override
	public void authorise()
	{
		
		try
		{
			Path store = Paths.get(Authorisation.class.getResource("/").toURI());
			
			// See if we already have stored an OAuth token.
			info = new File(store.toFile(), ".cliubuntuone");
			
			if (info.exists())
			{
				final Properties creds = new Properties();
				creds.load(new FileInputStream(info));
				oauthData = creds.getProperty("oauth");
			}
			else
			{
				acquirePermission();
				saveToken();
			}
			
			// OAuth data we received contains consumerKey, consumerSecret, tokenKey and tokenSecret.
			oauthAuthorizer = OAuthAuthorizer.getWithTokens(oauthData, new PlainTextMessageSigner());
			
			// Make sure our time is not off too much.
			OAuthAuthorizer.syncTimeWithU1(Ubuntu.httpClient);
		}
		catch (TimeDriftException e)
		{
			e.printStackTrace();
			System.err.println("Could not sync time with Ubuntu One. Sync with NTP?");
		}
		catch (IOException | URISyntaxException e)
		{
			e.printStackTrace();
		}
		
	}
	
	@Override
	public void acquirePermission()
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
		}
		catch (IOException e)
		{
			e.printStackTrace();
			System.err.println("Connection problem: " + e.getMessage());
		}
		catch (TimeDriftException e)
		{
			e.printStackTrace();
			System.err.println("Could not sync time with Ubuntu One. Sync with NTP?");
		}
		catch (U1PingException e)
		{
			e.printStackTrace();
			System.err.println("Could not authorize access to Ubuntu One. Please try again.");
		}
	}
	
	private AuthenticateResponse authenticate() throws AuthenticationException, IOException, TimeDriftException, U1PingException
	{
		final BasicAuthorizer basicAuthorizer = new BasicAuthorizer(username, password);
		
		final U1AuthAPI authApi = new U1AuthAPI(Ubuntu.class.getPackage().getName(), "1.0", Ubuntu.httpClient, basicAuthorizer);
		
		// Make sure your token name is in the format of 'Ubuntu One @ your_string"
		final AuthenticateResponse response = authApi.authenticate("Ubuntu One @ Overcast");
		authApi.setAuthorizer(new OAuthAuthorizer(response, new HmacSha1MessageSigner()));
		
		// Make sure our time is not off too much.
		OAuthAuthorizer.syncTimeWithU1(Ubuntu.httpClient);
		
		// Single Sign On has the token, let Ubuntu One know about it.
		authApi.pingUbuntuOne(username);
		
		return response;
	}
	
	@Override
	public void reacquirePermission()
	{}
	
	@Override
	public void saveToken()
	{
		
		// Save the token for later reuse. Usually a safer place than a plain text file is strongly advised.
		final Properties creds = new Properties();
		
		try
		{
			creds.put("oauth", oauthData);
			creds.store(new FileOutputStream(info), "");
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
	}
	
}
