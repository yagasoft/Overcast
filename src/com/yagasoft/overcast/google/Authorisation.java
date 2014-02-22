
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
import com.yagasoft.overcast.authorisation.OAuth;


public class Authorisation extends OAuth
{

	/** Directory to store user credentials. */
	protected Path							DATA_STORE_DIR;

	/**
	 * Global instance of the {@link DataStoreFactory}. The best practice is to
	 * make it a single globally shared instance across your application.
	 */
	protected FileDataStoreFactory			dataStoreFactory;

	protected Credential					credential;
	protected GoogleAuthorizationCodeFlow	flow;

	/**
	 * @see com.yagasoft.overcast.authorisation.Authorisation#authorise()
	 */
	@Override
	public void authorise()
	{
		try
		{
			DATA_STORE_DIR = Paths.get(Authorisation.class.getResource("/").toURI());

			dataStoreFactory = new FileDataStoreFactory(DATA_STORE_DIR.toFile());

			GoogleClientSecrets clientSecrets =
					GoogleClientSecrets.load(Google.JSON_FACTORY,
							new InputStreamReader(Authorisation.class.getResourceAsStream("/" + info.getFileName())));

			String clientId = clientSecrets.getDetails().getClientId();
			String clientSecret = clientSecrets.getDetails().getClientSecret();

			if (clientId.startsWith("Enter") || clientSecret.startsWith("Enter "))
			{
				System.out
						.println("Enter Client ID and Secret from https://code.google.com/apis/console/?api=drive "
								+ "into ./client_secrets.json");
				System.exit(1);
			}

			// set up authorisation code flow
			flow = new GoogleAuthorizationCodeFlow.Builder(Google.httpTransport, Google.JSON_FACTORY, clientSecrets,
					Collections.singleton(DriveScopes.DRIVE))
					.setDataStoreFactory(dataStoreFactory).build();

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
