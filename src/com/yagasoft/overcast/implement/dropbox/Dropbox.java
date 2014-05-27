/*
 * Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
 *
 *		The Modified MIT Licence (GPL v3 compatible)
 * 			Licence terms are in a separate file (LICENCE.md)
 *
 *		Project/File: Overcast/com.yagasoft.overcast.implement.dropbox/Dropbox.java
 *
 *			Modified: 25-May-2014 (23:11:28)
 *			   Using: Eclipse J-EE / JDK 7 / Windows 8.1 x64
 */

package com.yagasoft.overcast.implement.dropbox;


import java.util.Locale;

import com.dropbox.core.DbxAccountInfo;
import com.dropbox.core.DbxClient;
import com.dropbox.core.DbxEntry;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.yagasoft.logger.Logger;
import com.yagasoft.overcast.base.container.local.LocalFile;
import com.yagasoft.overcast.base.container.local.LocalFolder;
import com.yagasoft.overcast.base.container.operation.IOperationListener;
import com.yagasoft.overcast.base.container.operation.Operation;
import com.yagasoft.overcast.base.container.remote.RemoteFactory;
import com.yagasoft.overcast.base.container.transfer.event.TransferState;
import com.yagasoft.overcast.base.csp.CSP;
import com.yagasoft.overcast.exception.AuthorisationException;
import com.yagasoft.overcast.exception.CSPBuildException;
import com.yagasoft.overcast.exception.CreationException;
import com.yagasoft.overcast.exception.OperationException;
import com.yagasoft.overcast.exception.TransferException;
import com.yagasoft.overcast.implement.dropbox.authorisation.Authorisation;
import com.yagasoft.overcast.implement.dropbox.transfer.DownloadJob;
import com.yagasoft.overcast.implement.dropbox.transfer.Downloader;
import com.yagasoft.overcast.implement.dropbox.transfer.IProgressListener;
import com.yagasoft.overcast.implement.dropbox.transfer.UploadJob;
import com.yagasoft.overcast.implement.dropbox.transfer.Uploader;


/**
 * Class representing Dropbox. It handles authentication, transfer of files, and contains the root.
 */
public class Dropbox extends CSP<DbxEntry.File, Downloader, Uploader> implements IProgressListener
{

	/** The Dropbox singleton. */
	static private Dropbox				instance;

	/** Constant: application name. */
	static final String					APPLICATION_NAME	= "Overcast";

	/** The authorisation object. */
	static Authorisation				authorisation;

	/** The dropbox service object, which is used to call on any services. */
	static DbxClient					dropboxService;

	/** The remote file/folder factory. */
	static RemoteFactory<DbxEntry.Folder, RemoteFolder, DbxEntry.File, RemoteFile, Dropbox>	factory;

	/** User locale. */
	static final String					userLocale			= Locale.getDefault().toString();

	/** Request config. */
	static DbxRequestConfig				requestConfig;

	/**
	 * Instantiates a new Dropbox object.
	 *
	 * @param userID
	 *            User ID to identify this account.
	 * @param port
	 *            Port for server to receive access code -- any free port on localhost, but added to dev account.<br />
	 *            Please, refer to {@link Authorisation} for more details.
	 * @throws CSPBuildException
	 *             the CSP build exception
	 * @throws AuthorisationException
	 */
	private Dropbox(String userID, int port) throws CSPBuildException, AuthorisationException
	{
		Logger.info("building dropbox object");

		requestConfig = new DbxRequestConfig(userID, userLocale);

		// authenticate.
		authorisation = new Authorisation(userID, "dropbox", port);
		authorisation.authorise();

		// Create a DbxClient, which is what you use to make API calls.
		dropboxService = new DbxClient(requestConfig, authorisation.getAuthInfo().accessToken
				, authorisation.getAuthInfo().host);

		// initialise the remote file factory.
		factory = new RemoteFactory<DbxEntry.Folder, RemoteFolder, DbxEntry.File, RemoteFile, Dropbox>(
				this, RemoteFolder.class, RemoteFile.class, "");

		name = "Dropbox";

		Logger.info("done building dropbox");
	}

	/**
	 * Gets the single instance of Dropbox.
	 *
	 * @param userID
	 *            User ID to identify this account.
	 * @param port
	 *            Port for server to receive access code -- any free port on localhost, but added to dev account.<br />
	 *            Please, refer to {@link Authorisation} for more details.
	 * @return single instance of Dropbox
	 * @throws CSPBuildException
	 *             the CSP build exception
	 * @throws AuthorisationException
	 *             the authorisation exception
	 */
	public static Dropbox getInstance(String userID, int port) throws CSPBuildException, AuthorisationException
	{
		if (instance == null)
		{
			instance = new Dropbox(userID, port);
		}

		return instance;
	}

	/**
	 * @throws CreationException
	 * @see com.yagasoft.overcast.base.csp.CSP#initTree(IContentListener)
	 */
	@Override
	public void initTree(IOperationListener listener) throws OperationException
	{
		try
		{
			remoteFileTree = factory.createFolder();
			remoteFileTree.setPath("/");
			remoteFileTree.updateFromSource(false, false);

			if (listener != null)
			{
				remoteFileTree.addOperationListener(listener, Operation.ADD);
				remoteFileTree.addOperationListener(listener, Operation.REMOVE);
			}

			// buildFileTree(false);
		}
		catch (CreationException e)
		{
			Logger.error("can't initialise tree");
			Logger.except(e);
			e.printStackTrace();
		}
	}

	/**
	 * Calculate remote free space.
	 *
	 * @return Long
	 * @throws OperationException
	 *             the operation exception
	 * @see com.yagasoft.overcast.base.csp.CSP#calculateRemoteFreeSpace()
	 */
	@Override
	public long calculateRemoteFreeSpace() throws OperationException
	{
		Logger.info("getting dropbox freespace");

		try
		{
			DbxAccountInfo info = dropboxService.getAccountInfo();

			Logger.info("got dropbox freespace");

			return (info.quota.total - (info.quota.normal + info.quota.shared));
		}
		catch (DbxException e)
		{
			Logger.error("failed to get free space: Dropbox");
			Logger.except(e);
			e.printStackTrace();

			throw new OperationException("Couldn't determine free space. " + e.getMessage());
		}
	}

	/**
	 * @see com.yagasoft.overcast.base.csp.CSP#downloadProcess(com.yagasoft.overcast.base.container.remote.RemoteFile,
	 *      com.yagasoft.overcast.base.container.local.LocalFolder, boolean)
	 */
	@Override
	protected DownloadJob downloadProcess(com.yagasoft.overcast.base.container.remote.RemoteFile<?> file, LocalFolder parent
			, boolean overwrite) throws TransferException
	{
		// create a download job.
		DownloadJob downloadJob = new DownloadJob((RemoteFile) file, parent, overwrite, null, null);
		Downloader downloader = new Downloader(file.getPath(), parent.getPath(), downloadJob);
		downloadJob.setCspTransferer(downloader);
		downloadJob.setCanceller(downloader);
		downloader.addProgressListener(this);

		return downloadJob;
	}

	@Override
	protected void initiateDownload() throws TransferException
	{
		// download the file.
		DbxEntry.File file = currentDownloadJob.getCspTransferer().startDownload();

		// the operation wasn't cancelled ...
		if (file != null)
		{
			currentDownloadJob.success();
		}
	}

	@Override
	public void progressChanged(DownloadJob downloadJob, TransferState state, float progress)
	{
		switch (state)
		{
			case INITIALISED:
				currentDownloadJob.notifyProgressListeners(state, progress);
				break;

			case IN_PROGRESS:
				currentDownloadJob.progress(progress);
				break;

			case CANCELLED:
				currentDownloadJob.notifyProgressListeners(state, progress);
				break;

			default:
				break;
		}
	}

	@Override
	protected UploadJob uploadProcess(LocalFile file, com.yagasoft.overcast.base.container.remote.RemoteFolder<?> parent
			, boolean overwrite, com.yagasoft.overcast.base.container.remote.RemoteFile<?> remoteFile) throws TransferException
	{
		// create an upload job.
		UploadJob uploadJob = new UploadJob(file, (RemoteFile) remoteFile, (RemoteFolder) parent
				, overwrite, null, null);
		Uploader uploader = new Uploader(parent.getPath(), file.getPath(), uploadJob);
		uploadJob.setCspTransferer(uploader);
		uploadJob.setCanceller(uploader);
		uploader.addProgressListener(this);

		return uploadJob;
	}

	/**
	 * @see com.yagasoft.overcast.base.csp.CSP#initiateUpload()
	 */
	@Override
	protected void initiateUpload() throws TransferException
	{
		// upload the file and retrieve the result.
		DbxEntry.File file = currentUploadJob.getCspTransferer().startUpload();

		// the operation wasn't cancelled ...
		if (file != null)
		{
			currentUploadJob.success(file);
		}
	}

	/**
	 * @see com.yagasoft.overcast.implement.dropbox.transfer.IProgressListener#progressChanged(com.yagasoft.overcast.implement.dropbox.transfer.UploadJob,
	 *      com.yagasoft.overcast.base.container.transfer.event.TransferState, float)
	 */
	@Override
	public void progressChanged(UploadJob uploadJob, TransferState state, float progress)
	{
		switch (state)
		{
			case INITIALISED:
				currentUploadJob.notifyProgressListeners(state, progress);
				break;

			case IN_PROGRESS:
				currentUploadJob.progress(progress);
				break;

			case CANCELLED:
				currentUploadJob.notifyProgressListeners(state, progress);
				break;

			default:
				break;
		}
	}

	/**
	 * @see com.yagasoft.overcast.base.csp.CSP#getAbstractFactory()
	 */
	@Override
	public com.yagasoft.overcast.base.container.remote.RemoteFactory<?, ?, ?, ?, ?> getAbstractFactory()
	{
		return factory;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	// #region Getters and setters.
	// ======================================================================================

	/**
	 * Gets the factory.
	 *
	 * @return the factory
	 */
	public static RemoteFactory<DbxEntry.Folder, RemoteFolder, DbxEntry.File, RemoteFile, Dropbox> getFactory()
	{
		return factory;
	}

	/**
	 * @return the authorisation
	 */
	@Override
	public Authorisation getAuthorisation()
	{
		return authorisation;
	}

	/**
	 * @return the dropboxService
	 */
	public static DbxClient getDropboxService()
	{
		return dropboxService;
	}

	/**
	 * @return the requestConfig
	 */
	public static DbxRequestConfig getRequestConfig()
	{
		return requestConfig;
	}

	// ======================================================================================
	// #endregion Getters and setters.
	// //////////////////////////////////////////////////////////////////////////////////////

}
