
package com.yagasoft.overcast.google;


import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.yagasoft.overcast.CSP;


public class Google extends CSP
{

	/**
	 * Be sure to specify the name of your application. If the application name
	 * is {@code null} or
	 * blank, the application will log a warning. Suggested format is
	 * "MyCompany-ProductName/1.0".
	 */
	static final String			APPLICATION_NAME	= "Overcast";

	/** Global instance of the HTTP transport. */
	static HttpTransport		httpTransport;

	/** Global Drive API client. */
	static Drive				driveService;

	/** Global instance of the JSON factory. */
	static final JsonFactory	JSON_FACTORY		= JacksonFactory.getDefaultInstance();

	Authorisation				authorisation;

	public Google()
	{
		try
		{
			httpTransport = GoogleNetHttpTransport.newTrustedTransport();

			authorisation = new Authorisation();
			authorisation.setInfo(Paths.get(Authorisation.class.getResource("/client_secrets.json").toURI()));
			authorisation.authorise();

			// set up the global Drive instance
			driveService = new Drive.Builder(httpTransport, JSON_FACTORY, authorisation.credential).setApplicationName(
					APPLICATION_NAME).build();
		}
		catch (IOException | GeneralSecurityException | URISyntaxException e)
		{
			e.printStackTrace();
		}
	}

	public void buildFileTree(boolean recursive)
	{
		RemoteFolder root = new RemoteFolder();
		root.setId("0ByO8YVIZubxxdTZUNUhHYTB5bFE");
		root.buildTree(1);

//		for (Container<?> container : root.getChildrenList())
//		{
//			System.out.println(container.getName());
//		}

//		try
//		{
//		RemoteFolder root = new RemoteFolder(getDriveService());
//		root.setApiFile(new File());
//		root.setId("root");
//		root.update(recursive);
//
//		setFullTreeLoaded(recursive);
//
//		setRemoteFileTree(root);

//			Iterator<TreeNode> iterator = getRemoteFileTree().preorderIterator();
//
//			while (iterator.hasNext())
//			{
//				File file = driveService.files().get(((IRemote) iterator.next()).getId()).execute();
//				System.out.println("Title: " + file.getTitle());
//				System.out.println("Description: " + file.getDescription());
//				System.out.println("MIME type: " + file.getMimeType());
//				System.out.println("\n");
//			}
//		}
//		catch (IOException e)
//		{
//			e.printStackTrace();
//		}
	}

	/**
	 * @see com.yagasoft.overcast.CSP#calculateLocalFreeSpace()
	 */
	@Override
	public long calculateLocalFreeSpace()
	{
		return 0;
	}

	/**
	 * @see com.yagasoft.overcast.CSP#calculateRemoteFreeSpace()
	 */
	@Override
	public long calculateRemoteFreeSpace()
	{
		return 0;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	// #region Getters and setters.
	// ======================================================================================

	/**
	 * @return the httpTransport
	 */
	public static HttpTransport getHttpTransport()
	{
		return httpTransport;
	}

	/**
	 * @param httpTransport
	 *            the httpTransport to set
	 */
	public static void setHttpTransport(HttpTransport httpTransport)
	{
		Google.httpTransport = httpTransport;
	}

	/**
	 * @return the driveService
	 */
	public static Drive getDriveService()
	{
		return driveService;
	}

	/**
	 * @param driveService
	 *            the driveService to set
	 */
	public static void setDriveService(Drive driveService)
	{
		Google.driveService = driveService;
	}

	/**
	 * @return the jsonFactory
	 */
	public static JsonFactory getJsonFactory()
	{
		return JSON_FACTORY;
	}

	// ======================================================================================
	// #endregion Getters and setters.
	// //////////////////////////////////////////////////////////////////////////////////////

}
