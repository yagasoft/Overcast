/*
 * Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
 *
 *		The Modified MIT Licence (GPL v3 compatible)
 * 			Licence terms are in a separate file (LICENCE.md)
 *
 *		Project/File: Overcast/com.yagasoft.overcast.implement.ubuntu/Ubuntu.java
 *
 *			Modified: 22-Apr-2014 (13:38:36)
 *			   Using: Eclipse J-EE / JDK 7 / Windows 8.1 x64
 */

package com.yagasoft.overcast.implement.ubuntu;


import java.util.ArrayList;
import java.util.Arrays;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;

import com.ubuntuone.api.files.U1FileAPI;
import com.ubuntuone.api.files.model.U1File;
import com.ubuntuone.api.files.model.U1Node;
import com.ubuntuone.api.files.model.U1User;
import com.ubuntuone.api.files.request.U1DownloadListener;
import com.ubuntuone.api.files.request.U1UploadListener;
import com.ubuntuone.api.files.util.U1CancelTrigger;
import com.ubuntuone.api.files.util.U1Failure;
import com.ubuntuone.api.files.util.U1RequestListener.U1UserRequestListener;
import com.yagasoft.overcast.base.container.local.LocalFile;
import com.yagasoft.overcast.base.container.local.LocalFolder;
import com.yagasoft.overcast.base.container.operation.IOperationListener;
import com.yagasoft.overcast.base.container.operation.OperationEvent;
import com.yagasoft.overcast.base.container.transfer.ITransferProgressListener;
import com.yagasoft.overcast.base.container.transfer.TransferState;
import com.yagasoft.overcast.base.csp.CSP;
import com.yagasoft.overcast.exception.AuthorisationException;
import com.yagasoft.overcast.exception.CSPBuildException;
import com.yagasoft.overcast.exception.CreationException;
import com.yagasoft.overcast.exception.OperationException;
import com.yagasoft.overcast.exception.TransferException;


/**
 * Class representing Ubuntu One. It handles authentication, transfer of files, and contains the root.
 */
public class Ubuntu extends CSP<U1File, U1DownloadListener, U1UploadListener>
{
	
	static private Ubuntu	instance;
	
	/** The authorisation. */
	private Authorisation	authorisation;
	
	/** The http client. */
	static HttpClient		httpClient;
	
	/** Files API instance. */
	static U1FileAPI		ubuntuService;
	
	/** The remote file factory. */
	static RemoteFactory	factory;
	
	private boolean			operationSuccess	= false;
	
	/**
	 * Instantiates a new Ubuntu.
	 * 
	 * @throws CSPBuildException
	 */
	public Ubuntu(String username, String password) throws CSPBuildException
	{
		// get the communication medium.
		httpClient = new DefaultHttpClient(new ThreadSafeClientConnManager());
		
		// authorise using the username and password.
		authorisation = new Authorisation(username, password);
		
		try
		{
			authorisation.authorise();
		}
		catch (AuthorisationException e1)
		{
			e1.printStackTrace();
			throw new CSPBuildException("Can't construct CSP object!");
		}
		
		// create the Ubuntu One service to be used for all operations.
		ubuntuService = new U1FileAPI(Ubuntu.class.getPackage().getName(), "1.0", httpClient, authorisation.getOauthAuthorizer());
		
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
		
		// create the factory.
		factory = new RemoteFactory(this);
		
		name = "Ubuntu One";
	}
	
	public static Ubuntu getInstance(String username, String password) throws CSPBuildException, AuthorisationException
	{
		if (instance == null)
		{
			instance = new Ubuntu(username, password);
		}
		
		return instance;
	}
	
	@Override
	public void initTree() throws OperationException
	{
		try
		{
			remoteFileTree = factory.createFolder();
			remoteFileTree.setPath("/");
			remoteFileTree.updateFromSource(false, false);
			buildFileTree(false);
		}
		catch (CreationException e)
		{
			e.printStackTrace();
		}
	}
	
//	@Override
//	public void buildFileTree(boolean recursively)
//	{
//		remoteFileTree.buildTree(recursively);
//	}
	
	/**
	 * @see com.yagasoft.overcast.base.csp.CSP#download(com.yagasoft.overcast.base.container.remote.RemoteFile,
	 *      com.yagasoft.overcast.base.container.local.LocalFolder, boolean,
	 *      com.yagasoft.overcast.base.container.transfer.ITransferProgressListener)
	 */
	@Override
	public DownloadJob download(com.yagasoft.overcast.base.container.remote.RemoteFile<?> file, LocalFolder parent,
			boolean overwrite
			, ITransferProgressListener listener) throws TransferException
	{
		// overwrite if required.
		for (com.yagasoft.overcast.base.container.File<?> child : parent.getFilesArray())
		{
			if (file.getName().equals(child.getName()))
			{
				if (overwrite)
				{
					try
					{
						child.delete(new IOperationListener()
						{
							
							@Override
							public void operationProgressChanged(OperationEvent event)
							{}
						});
					}
					catch (OperationException e)
					{
						e.printStackTrace();
					}
				}
				else
				{
					throw new TransferException("File exists!");
				}
			}
		}
		
		DownloadJob downloadJob = new DownloadJob((RemoteFile) file, parent, overwrite
				, new U1DownloadListener()
				{
					
					@Override
					public void onStart()
					{
						currentDownloadJob.notifyProgressListeners(TransferState.INITIALISED, 0.0f);
					}
					
					@Override
					public void onProgress(long bytes, long total)
					{
						currentDownloadJob.progress(bytes / (float) total);
					}
					
					@Override
					public void onSuccess()
					{
						currentDownloadJob.success();
					}
					
					@Override
					public void onUbuntuOneFailure(U1Failure failure)
					{
						currentDownloadJob.failure();
						System.err.println("Ubuntu One failure: " + failure);
					}
					
					@Override
					public void onFailure(U1Failure failure)
					{
						currentDownloadJob.failure();
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
						currentDownloadJob = null;
						nextDownloadJob();
					}
				}
				, new U1CancelTrigger());
		downloadQueue.add(downloadJob);		// add to queue.
		
		downloadJob.addProgressListener(listener);
		
		nextDownloadJob();		// check if immediate execution is possible.
		
		return downloadJob;
	}
	
	@Override
	protected void initiateDownload() throws TransferException
	{
		// download the file.
		ubuntuService.downloadFile(((RemoteFile) currentDownloadJob.getRemoteFile()).getUbuntuPath()
				, currentDownloadJob.getLocalFile().getPath()
				, currentDownloadJob.getCspTransferer(), null);
	}
	
	/**
	 * Cancel current download.
	 */
	@Override
	public void cancelCurrentDownload()
	{
		currentDownloadJob.cancelTransfer();
	}
	
	/**
	 * @see com.yagasoft.overcast.base.csp.CSP#upload(com.yagasoft.overcast.base.container.local.LocalFile,
	 *      com.yagasoft.overcast.base.container.remote.RemoteFolder, boolean,
	 *      com.yagasoft.overcast.base.container.transfer.ITransferProgressListener)
	 */
	@Override
	public UploadJob upload(final LocalFile file, com.yagasoft.overcast.base.container.remote.RemoteFolder<?> parent
			, boolean overwrite, ITransferProgressListener listener) throws TransferException
	{
		// overwrite if required.
		for (com.yagasoft.overcast.base.container.File<?> child : parent.getFilesArray())
		{
			if (child.getName().equals(file.getName()))
			{
				if (overwrite)
				{
					try
					{
						child.delete(new IOperationListener()
						{
							
							@Override
							public void operationProgressChanged(OperationEvent event)
							{}
						});
					}
					catch (OperationException e)
					{
						e.printStackTrace();
					}
				}
				else
				{
					throw new TransferException("File exists!");
				}
			}
		}
		
		RemoteFile remoteFile;
		try
		{
			remoteFile = factory.createFile();
		}
		catch (CreationException e)
		{
			e.printStackTrace();
			throw new TransferException("Problem!");
		}
		
		UploadJob uploadJob = new UploadJob(
				file, remoteFile, (RemoteFolder) parent, overwrite
				, new U1UploadListener()
				{
					
					@Override
					public void onStart()
					{
						currentUploadJob.notifyProgressListeners(TransferState.INITIALISED, 0.0f);
					}
					
					@Override
					public void onProgress(long bytes, long total)
					{
						currentUploadJob.progress(bytes / (float) total);
					}
					
					@Override
					public void onSuccess(U1Node node)
					{
						currentUploadJob.success((U1File) node);
					}
					
					@Override
					public void onUbuntuOneFailure(U1Failure failure)
					{
						currentUploadJob.failure();
						System.err.println("Ubuntu One failure: " + failure);
					}
					
					@Override
					public void onFailure(U1Failure failure)
					{
						currentUploadJob.failure();
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
						resetUpload();
					}
				}
				, new U1CancelTrigger());
		uploadQueue.add(uploadJob);
		
		uploadJob.addProgressListener(listener);
		
		nextUploadJob();
		
		return uploadJob;
	}
	
	/**
	 * @see com.yagasoft.overcast.base.csp.CSP#initiateUpload()
	 */
	@Override
	protected void initiateUpload() throws TransferException
	{
		ubuntuService.uploadFile(currentUploadJob.getLocalFile().getPath(), currentUploadJob.getLocalFile().getType()
				, ((RemoteFolder) currentUploadJob.getParent()).getUbuntuPath() + "/"
						+ currentUploadJob.getLocalFile().getName(), true,
				false
				, currentUploadJob.getCspTransferer(), ((UploadJob) currentUploadJob).getCanceller());
	}
	
	/**
	 * Cancel current upload.
	 */
	@Override
	public void cancelCurrentUpload()
	{
		currentUploadJob.cancelTransfer();
	}
	
	/**
	 * @see com.yagasoft.overcast.base.csp.CSP#calculateRemoteFreeSpace()
	 */
	@Override
	public long calculateRemoteFreeSpace() throws OperationException
	{
		operationSuccess = false;
		
		ubuntuService.getUser(new U1UserRequestListener()
		{
			
			@Override
			public void onStart()
			{}
			
			@Override
			public void onSuccess(U1User result)
			{
				remoteFreeSpace = result.getMaxBytes() - result.getUsedBytes();
				operationSuccess = true;
			}
			
			@Override
			public void onUbuntuOneFailure(U1Failure failure)
			{}
			
			@Override
			public void onFailure(U1Failure failure)
			{}
			
			@Override
			public void onFinish()
			{}
		});
		
		if ( !operationSuccess)
		{
			throw new OperationException("Failed to get free space.");
		}
		else
		{
			operationSuccess = false;
			
			return remoteFreeSpace;
		}
	}
	
	/**
	 * Overrides splitting the path in {@link CSP} to remove the prefix.
	 * 
	 * @param path
	 *            Path.
	 * @return Array list
	 */
	@Override
	public ArrayList<String> splitPath(String path)
	{
		ArrayList<String> splitPath = new ArrayList<String>(Arrays.asList(path.split("/")));
		
		if (splitPath.get(0).equals(""))
		{
			splitPath.remove(0);
		}
		
		if (splitPath.get(0).equals("~"))
		{
			splitPath.remove(0);
		}
		
		if (splitPath.get(0).equals("Ubuntu One"))
		{
			splitPath.remove(0);
		}
		
		return splitPath;
	}
	
	@Override
	public com.yagasoft.overcast.base.container.remote.RemoteFactory<?, ?, ?, ?> getAbstractFactory()
	{
		return factory;
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
