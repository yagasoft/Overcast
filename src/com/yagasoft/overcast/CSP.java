/*
 * Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
 *
 *		Modified MIT License (GPL v3 compatible)
 * 			License terms are in a separate file (license.txt)
 *
 */

package com.yagasoft.overcast;


import java.util.LinkedList;
import java.util.Queue;

import com.yagasoft.overcast.authorisation.Authorisation;
import com.yagasoft.overcast.container.local.LocalFile;
import com.yagasoft.overcast.container.local.LocalFolder;
import com.yagasoft.overcast.container.remote.RemoteFile;
import com.yagasoft.overcast.container.remote.RemoteFolder;
import com.yagasoft.overcast.container.transfer.DownloadJob;
import com.yagasoft.overcast.container.transfer.ITransferProgressListener;
import com.yagasoft.overcast.container.transfer.UploadJob;
import com.yagasoft.overcast.exception.TransferException;


/**
 * The class representing the Cloud Storage Provider.<br />
 * It's abstract, and the upload and download must be implemented first.
 *
 * @param <SourceFileType>
 *            The source file type (file type from the original CSP API) must be passed to this class.<br />
 *            It's needed to assist in creating the {@link RemoteFile}.
 * @param <DownloaderType>
 *            The type of the downloader (from the original CSP API). Used to create the job.
 * @param <UploaderType>
 *            The type of the uploader (from the original CSP API). Used to create the job.
 */
public abstract class CSP<SourceFileType, DownloaderType, UploaderType>
{

	/** Name of the CSP. Can be used to display next to files in an application, for example. */
	protected String											name;

	/** Authorisation object. */
	protected Authorisation										authorisation;

	/** Root of the local file tree. */
	protected LocalFolder										localFileTree;

	/** Root of the remote file tree. */
	protected RemoteFolder<?>									remoteFileTree;

	/** Is the local file tree fully read in memory. */
	protected boolean											fullLocalTreeLoaded;

	/** Is the remote file tree fully read in memory. */
	protected boolean											fullRemoteTreeLoaded;

	/** Local free space. */
	protected long												localFreeSpace;

	/** Remote free space. */
	protected long												remoteFreeSpace;

	/** Download queue. */
	protected Queue<DownloadJob<DownloaderType>>				downloadQueue	= new LinkedList<DownloadJob<DownloaderType>>();

	/** Current download job. */
	protected DownloadJob<DownloaderType>						currentDownloadJob;

	/** Upload queue. */
	protected Queue<UploadJob<UploaderType, SourceFileType>>	uploadQueue		= new LinkedList<UploadJob<UploaderType, SourceFileType>>();

	/** Current upload job. */
	protected UploadJob<UploaderType, SourceFileType>			currentUploadJob;

	// add a RemoteFactory object in the subclass.

	/**
	 * Initialises the tree -- only reads the root from the source, along with the contents (only single level).
	 */
	public abstract void initTree();

	/**
	 * Builds the file tree by starting from the root and going down.
	 *
	 * @param recursively
	 *            If true, then build all the levels possible under the root.
	 */
	public abstract void buildFileTree(boolean recursively);

	/**
	 * Calculate local free space available on the local disk (the one the root resides on).
	 *
	 * @return the free space in bytes
	 */
	public long calculateLocalFreeSpace()
	{
		return localFileTree.calculateSize();
	}

	/**
	 * Calculate remote free space available on the CSP.
	 *
	 * @return the free space in bytes.
	 */
	public long calculateRemoteFreeSpace()
	{
		return remoteFileTree.calculateSize();
	}

	/**
	 * Download the folder (passed) from the server.<br />
	 * It creates the folder locally and all sub-folders if necessary.
	 *
	 * @param folder
	 *            Folder to download.
	 * @param parent
	 *            The local folder to download to. Must pass a {@link LocalFolder} with the path initialised in it.
	 * @param overwrite
	 *            Whether to overwrite any existing files and folders on the local disk or not.
	 * @param listener
	 *            Object listening to the changes in the transfer state.
	 * @param object
	 *            Object passed by the initialiser to be passed back on state change. It can be used as a kind of "call-back" or
	 *            something; the sender of this object can cast it back and use it as seen fit.
	 */
	public abstract void download(RemoteFolder<?> folder, LocalFolder parent, boolean overwrite
			, ITransferProgressListener listener, Object object);

	/**
	 * Download the file (passed) from the server.
	 *
	 * @param file
	 *            File to download.
	 * @param parent
	 *            The local folder to download to. Must pass a {@link LocalFolder} with the path initialised in it.
	 * @param overwrite
	 *            Whether to overwrite existing file on the local disk or not.
	 * @param listener
	 *            Object listening to the changes in the transfer state.
	 * @param object
	 *            Object passed by the initialiser to be passed back on state change. It can be used as a kind of "call-back" or
	 *            something; the sender of this object can cast it back and use it as seen fit.
	 * @throws TransferException
	 *             A problem occurred during the transfer of the file.
	 */
	public abstract void download(RemoteFile<SourceFileType> file, LocalFolder parent, boolean overwrite,
			ITransferProgressListener listener
			, Object object)
			throws TransferException;

	/**
	 * If the queue has a job, set it as the current one after removing it from the queue, and then start the job.
	 */
	public abstract void nextDownloadJob();

	/**
	 * Upload the folder (passed) to the server.<br />
	 * It creates the folder remotely and all sub-folders if necessary.
	 *
	 * @param folder
	 *            Folder to upload.
	 * @param parent
	 *            The remote folder to upload to. Must pass a {@link RemoteFolder} with the path initialised in it.
	 * @param overwrite
	 *            Whether to overwrite any existing files and folders on the server or not.
	 * @param listener
	 *            Object listening to the changes in the transfer state.
	 * @param object
	 *            Object passed by the initialiser to be passed back on state change. It can be used as a kind of "call-back" or
	 *            something; the sender of this object can cast it back and use it as seen fit.
	 */
	public abstract void upload(LocalFolder folder, RemoteFolder<?> parent, boolean overwrite
			, ITransferProgressListener listener, Object object);

	/**
	 * Upload the file (passed) to the server.
	 *
	 * @param file
	 *            File to upload..
	 * @param parent
	 *            The remote folder to upload to. Must pass a {@link RemoteFolder} with the path initialised in it.
	 * @param overwrite
	 *            Whether to overwrite existing file on the server or not.
	 * @param listener
	 *            Object listening to the changes in the transfer state.
	 * @param object
	 *            Object passed by the initialiser to be passed back on state change. It can be used as a kind of "call-back" or
	 *            something; the sender of this object can cast it back and use it as seen fit.
	 * @throws TransferException
	 *             A problem occurred during the transfer of the file.
	 */
	public abstract void upload(LocalFile file, RemoteFolder<?> parent, boolean overwrite, ITransferProgressListener listener
			, Object object)
			throws TransferException;

	/**
	 * If the queue has a job, set it as the current one after removing it from the queue, and then start the job.
	 */
	public abstract void nextUploadJob();

	// //////////////////////////////////////////////////////////////////////////////////////
	// #region Getters and setters.
	// ======================================================================================

	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Sets the name.
	 *
	 * @param name
	 *            the name to set
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * Gets the authorisation.
	 *
	 * @return the authorisation
	 */
	public Authorisation getAuthorisation()
	{
		return authorisation;
	}

	/**
	 * Sets the authorisation.
	 *
	 * @param value
	 *            the new authorisation
	 */
	public void setAuthorisation(Authorisation value)
	{
		authorisation = value;
	}

	/**
	 * Gets the local file tree.
	 *
	 * @return the localFileTree
	 */
	public LocalFolder getLocalFileTree()
	{
		return localFileTree;
	}

	/**
	 * Sets the local file tree.
	 *
	 * @param localFileTree
	 *            the localFileTree to set
	 */
	public void setLocalFileTree(LocalFolder localFileTree)
	{
		this.localFileTree = localFileTree;
	}

	/**
	 * Gets the remote file tree.
	 *
	 * @return the remote file tree
	 */
	public RemoteFolder<?> getRemoteFileTree()
	{
		return remoteFileTree;
	}

	/**
	 * Sets the remote file tree.
	 *
	 * @param value
	 *            the new remote file tree
	 */
	public void setRemoteFileTree(RemoteFolder<?> value)
	{
		remoteFileTree = value;
	}

	/**
	 * Checks if is full local tree loaded.
	 *
	 * @return the fullLocalTreeLoaded
	 */
	public boolean isFullLocalTreeLoaded()
	{
		return fullLocalTreeLoaded;
	}

	/**
	 * Sets the full local tree loaded.
	 *
	 * @param fullLocalTreeLoaded
	 *            the fullLocalTreeLoaded to set
	 */
	public void setFullLocalTreeLoaded(boolean fullLocalTreeLoaded)
	{
		this.fullLocalTreeLoaded = fullLocalTreeLoaded;
	}

	/**
	 * Checks if is full remote tree loaded.
	 *
	 * @return the fullRemoteTreeLoaded
	 */
	public boolean isFullRemoteTreeLoaded()
	{
		return fullRemoteTreeLoaded;
	}

	/**
	 * Sets the full remote tree loaded.
	 *
	 * @param fullRemoteTreeLoaded
	 *            the fullRemoteTreeLoaded to set
	 */
	public void setFullRemoteTreeLoaded(boolean fullRemoteTreeLoaded)
	{
		this.fullRemoteTreeLoaded = fullRemoteTreeLoaded;
	}

	/**
	 * Gets the local free space.
	 *
	 * @return the localFreeSpace
	 */
	public long getLocalFreeSpace()
	{
		return localFreeSpace;
	}

	/**
	 * Sets the local free space.
	 *
	 * @param localFreeSpace
	 *            the localFreeSpace to set
	 */
	public void setLocalFreeSpace(long localFreeSpace)
	{
		this.localFreeSpace = localFreeSpace;
	}

	/**
	 * Gets the remote free space.
	 *
	 * @return the remoteFreeSpace
	 */
	public long getRemoteFreeSpace()
	{
		return remoteFreeSpace;
	}

	/**
	 * Sets the remote free space.
	 *
	 * @param remoteFreeSpace
	 *            the remoteFreeSpace to set
	 */
	public void setRemoteFreeSpace(long remoteFreeSpace)
	{
		this.remoteFreeSpace = remoteFreeSpace;
	}

	// ======================================================================================
	// #endregion Getters and setters.
	// //////////////////////////////////////////////////////////////////////////////////////

}
