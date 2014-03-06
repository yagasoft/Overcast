
package com.yagasoft.overcast.ubuntu;


import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;

import com.ubuntuone.api.files.U1FileAPI;
import com.ubuntuone.api.files.model.U1File;
import com.ubuntuone.api.files.model.U1Node;
import com.ubuntuone.api.files.request.U1UploadListener;
import com.ubuntuone.api.files.util.U1Failure;
import com.yagasoft.overcast.CSP;
import com.yagasoft.overcast.container.local.LocalFile;
import com.yagasoft.overcast.container.local.LocalFolder;
import com.yagasoft.overcast.container.remote.RemoteFile;
import com.yagasoft.overcast.container.transfer.ITransferProgressListener;
import com.yagasoft.overcast.container.transfer.ITransferProgressListener.TransferState;
import com.yagasoft.overcast.container.transfer.UploadJob;
import com.yagasoft.overcast.exception.TransferException;


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
		remoteFileTree.buildTree(recursively ? Integer.MAX_VALUE : 0);
	}
	
	/**
	 * @see com.yagasoft.overcast.CSP#download(com.yagasoft.overcast.container.remote.RemoteFolder,
	 *      com.yagasoft.overcast.container.local.LocalFolder, boolean,
	 *      com.yagasoft.overcast.container.transfer.ITransferProgressListener, java.lang.Object)
	 */
	@Override
	public void download(com.yagasoft.overcast.container.remote.RemoteFolder<?> folder, LocalFolder parent, boolean overwrite,
			ITransferProgressListener listener, Object object)
	{}
	
	/**
	 * @see com.yagasoft.overcast.CSP#download(com.yagasoft.overcast.container.remote.RemoteFile,
	 *      com.yagasoft.overcast.container.local.LocalFolder, boolean,
	 *      com.yagasoft.overcast.container.transfer.ITransferProgressListener, java.lang.Object)
	 */
	@Override
	public void download(RemoteFile<?> file, LocalFolder parent, boolean overwrite, ITransferProgressListener listener,
			Object object) throws TransferException
	{}
	
	/**
	 * @see com.yagasoft.overcast.CSP#nextDownloadJob()
	 */
	@Override
	public void nextDownloadJob()
	{}
	
	/**
	 * @see com.yagasoft.overcast.CSP#upload(com.yagasoft.overcast.container.local.LocalFolder,
	 *      com.yagasoft.overcast.container.remote.RemoteFolder, boolean,
	 *      com.yagasoft.overcast.container.transfer.ITransferProgressListener, java.lang.Object)
	 */
	@Override
	public void upload(LocalFolder folder, com.yagasoft.overcast.container.remote.RemoteFolder<?> parent, boolean overwrite,
			ITransferProgressListener listener, Object object)
	{}
	
	/**
	 * @see com.yagasoft.overcast.CSP#upload(com.yagasoft.overcast.container.local.LocalFile,
	 *      com.yagasoft.overcast.container.remote.RemoteFolder, boolean,
	 *      com.yagasoft.overcast.container.transfer.ITransferProgressListener, java.lang.Object)
	 */
	@Override
	public void upload(final LocalFile file, com.yagasoft.overcast.container.remote.RemoteFolder<?> parent, boolean overwrite,
			ITransferProgressListener listener, Object object) throws TransferException
	{
		for (com.yagasoft.overcast.container.File<?> child : parent.getFilesArray())
		{
			if (child.getName().equals(file.getName()))
			{
				if (overwrite)
				{
					child.delete();
				}
				else
				{
					throw new TransferException("File exists!");
				}
			}
		}
		
		file.addProgressListener(listener, object);
		
		UploadJob<U1UploadListener> uploadJob = new UploadJob<U1UploadListener>(file, parent, overwrite
				, new U1UploadListener()
				{
					
					@Override
					public void onStart()
					{
						currentUploadJob.getFile().notifyListeners(TransferState.IN_PROGRESS, 0.0f);
					}
					
					@Override
					public void onProgress(long bytes, long total)
					{
						currentUploadJob.getFile().notifyListeners(TransferState.IN_PROGRESS, bytes / (float) total);
					}
					
					@Override
					public void onSuccess(U1Node node)
					{
						currentUploadJob.getFile().notifyListeners(TransferState.COMPLETED, 1.0f);
						currentUploadJob.getParent().add(factory.createFile((U1File) node, false));
					}
					
					@Override
					public void onUbuntuOneFailure(U1Failure failure)
					{
						System.err.println("Ubuntu One failure: " + failure);
					}
					
					@Override
					public void onFailure(U1Failure failure)
					{
						System.err.println("Generic failure: " + failure);
					}
					
					@Override
					public void onCancel()
					{
						System.err.println("Upload canceled!");
					}
					
					@Override
					public void onFinish()
					{
						currentUploadJob = null;
						nextUploadJob();
					}
				});
		uploadQueue.add(uploadJob);
		
		nextUploadJob();
	}
	
	@Override
	public void nextUploadJob()
	{
		if (currentUploadJob == null)
		{
			currentUploadJob = uploadQueue.remove();
			
			ubuntuService.uploadFile(currentUploadJob.getFile().getPath(), currentUploadJob.getFile().getType()
					, currentUploadJob.getParent().getPath() + "/" + currentUploadJob.getFile().getName(), true, false
					, (U1UploadListener) currentUploadJob.getCspUploader(), null);
		}
	}
	
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
