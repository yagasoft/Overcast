
package com.yagasoft.overcast.ubuntu;


import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;

import com.ubuntuone.api.files.U1FileAPI;
import com.yagasoft.overcast.CSP;
import com.yagasoft.overcast.container.ITransferProgressListener;
import com.yagasoft.overcast.container.LocalFile;
import com.yagasoft.overcast.container.LocalFolder;


public class Ubuntu extends CSP
{
	
	private Authorisation	authorisation;
	static HttpClient		httpClient;
	/** Files API instance. */
	static U1FileAPI		ubuntuService;
	
	static RemoteFactory	factory;
	
	/**
	 *
	 */
	public Ubuntu()
	{
		httpClient = new DefaultHttpClient(new ThreadSafeClientConnManager());
		
		authorisation = new Authorisation("os1983@gmail.com", "i018AiOOU9geK");
		authorisation.authorise();
		
		// We are all set, ready to talk to Ubuntu One file storage API.
		ubuntuService = new U1FileAPI(Ubuntu.class.getPackage().getName(), "1.0", httpClient, authorisation.oauthAuthorizer);
		
//		ubuntuService.getNode("/~/Ubuntu One", new U1NodeListener()
//		{
//
//			@Override
//			public void onSuccess(U1Node node)
//			{
//				System.out.println(node.getPath());
//			}
//
//			@Override
//			public void onUbuntuOneFailure(U1Failure failure)
//			{
//				System.err.println("Ubuntu One error: " + failure.getMessage());
//			}
//
//			@Override
//			public void onFailure(U1Failure failure)
//			{
//				System.err.println("General error: " + failure.getMessage());
//			}
//		});
		
//		ubuntuService.listDirectory("/~/Ubuntu One", new U1NodeRequestListener()
//		{
//			
//			@Override
//			public void onStart()
//			{}
//			
//			@Override
//			public void onSuccess(U1Node result)
//			{
//				System.out.println(result.getKey() + " => " + result.getResourcePath() + " : " + result.getKind());
//			}
//			
//			@Override
//			public void onFinish()
//			{}
//			
//			@Override
//			public void onUbuntuOneFailure(U1Failure failure)
//			{
//				System.err.println("Ubuntu One failure: " + failure);
//			}
//			
//			@Override
//			public void onFailure(U1Failure failure)
//			{
//				System.err.println("General failure: " + failure);
//			}
//		});
		
		factory = new RemoteFactory(this);
	}
	
	@Override
	public void initTree()
	{}
	
	@Override
	public void buildFileTree(boolean recursively)
	{
		remoteFileTree = new RemoteFolder();
		remoteFileTree.setPath("/~/Ubuntu One");
		remoteFileTree.updateFromSource();
		remoteFileTree.buildTree(recursively ? Integer.MAX_VALUE : 1);
	}
	
	/**
	 * @see com.yagasoft.overcast.CSP#upload(com.yagasoft.overcast.container.LocalFolder,
	 *      com.yagasoft.overcast.container.RemoteFolder, boolean, com.yagasoft.overcast.container.ITransferProgressListener,
	 *      java.lang.Object)
	 */
	@Override
	public void upload(LocalFolder folder, com.yagasoft.overcast.container.RemoteFolder<?> parent, boolean overwrite,
			ITransferProgressListener listener, Object object)
	{}
	
	/**
	 * @see com.yagasoft.overcast.CSP#upload(com.yagasoft.overcast.container.LocalFile,
	 *      com.yagasoft.overcast.container.RemoteFolder, boolean, com.yagasoft.overcast.container.ITransferProgressListener,
	 *      java.lang.Object)
	 */
	@Override
	public void upload(LocalFile file, com.yagasoft.overcast.container.RemoteFolder<?> parent, boolean overwrite,
			ITransferProgressListener listener, Object object) throws Exception
	{}
	
	@Override
	public void nextUploadJob()
	{}
	
	// --------------------------------------------------------------------------------------
	// #region Getters and setters.
	
	/**
	 * @return the ubuntuService
	 */
	public static U1FileAPI getUbuntuService()
	{
		return ubuntuService;
	}
	
	/**
	 * @param ubuntuService
	 *            the ubuntuService to set
	 */
	public static void setUbuntuService(U1FileAPI ubuntuService)
	{
		Ubuntu.ubuntuService = ubuntuService;
	}
	
	/**
	 * @return the factory
	 */
	public static RemoteFactory getFactory()
	{
		return factory;
	}
	
	/**
	 * @param factory
	 *            the factory to set
	 */
	public static void setFactory(RemoteFactory factory)
	{
		Ubuntu.factory = factory;
	}
	
	/**
	 * @return the httpClient
	 */
	public static HttpClient getHttpClient()
	{
		return httpClient;
	}
	
	/**
	 * @param httpClient
	 *            the httpClient to set
	 */
	public static void setHttpClient(HttpClient httpClient)
	{
		Ubuntu.httpClient = httpClient;
	}
	
	// #endregion Getters and setters.
	// --------------------------------------------------------------------------------------
	
}
