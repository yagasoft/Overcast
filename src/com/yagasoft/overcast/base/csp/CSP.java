/*
 * Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
 *
 *		The Modified MIT Licence (GPL v3 compatible)
 * 			Licence terms are in a separate file (LICENCE.md)
 *
 *		Project/File: Overcast/com.yagasoft.overcast.base.csp/CSP.java
 *
 *			Modified: 25-May-2014 (22:45:26)
 *			   Using: Eclipse J-EE / JDK 7 / Windows 8.1 x64
 */

package com.yagasoft.overcast.base.csp;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import com.yagasoft.logger.Logger;
import com.yagasoft.overcast.base.container.Container;
import com.yagasoft.overcast.base.container.Folder;
import com.yagasoft.overcast.base.container.local.LocalFile;
import com.yagasoft.overcast.base.container.local.LocalFolder;
import com.yagasoft.overcast.base.container.operation.IOperationListener;
import com.yagasoft.overcast.base.container.remote.RemoteFactory;
import com.yagasoft.overcast.base.container.remote.RemoteFile;
import com.yagasoft.overcast.base.container.remote.RemoteFolder;
import com.yagasoft.overcast.base.container.transfer.DownloadJob;
import com.yagasoft.overcast.base.container.transfer.TransferJob;
import com.yagasoft.overcast.base.container.transfer.UploadJob;
import com.yagasoft.overcast.base.container.transfer.event.ITransferProgressListener;
import com.yagasoft.overcast.base.csp.authorisation.Authorisation;
import com.yagasoft.overcast.exception.CSPBuildException;
import com.yagasoft.overcast.exception.CreationException;
import com.yagasoft.overcast.exception.OperationException;
import com.yagasoft.overcast.exception.TransferException;


/**
 * The class representing the Cloud Storage Provider.<br />
 * The constructors should throw a {@link CSPBuildException} if it can't go through to the end.
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

	/** Name of the CSP. Can be used to be displayed next to files in an application, for example. */
	protected String											name;

	/** Authorisation object. */
	protected Authorisation										authorisation;

	/** Root of the local file tree. */
	protected LocalFolder										localFileTree;

	/** Root of the remote file tree. */
	protected RemoteFolder<?>									remoteFileTree;

	/** Is the local file tree fully loaded in memory. */
	protected boolean											fullLocalTreeLoaded;

	/** Is the remote file tree fully loaded in memory. */
	protected boolean											fullRemoteTreeLoaded;

	/** Remote free space. */
	protected long												remoteFreeSpace;

	/** Download queue. */
	protected Queue<DownloadJob<DownloaderType>>				downloadQueue	= new LinkedList<DownloadJob<DownloaderType>>();

	/** Current download job. */
	protected DownloadJob<DownloaderType>						currentDownloadJob;

	/** Current download thread. */
	protected Thread											currentDownloadThread;

	/** Upload queue. */
	protected Queue<UploadJob<UploaderType, SourceFileType>>	uploadQueue		= new LinkedList<UploadJob<UploaderType, SourceFileType>>();

	/** Current upload job. */
	protected UploadJob<UploaderType, SourceFileType>			currentUploadJob;

	/** Current upload thread. */
	protected Thread											currentUploadThread;

	// you should add a RemoteFactory object in the subclasses.

	/**
	 * Initialises the tree -- only reads the root's own info but none of the children.
	 *
	 * @param listener
	 *            the listener to the changes
	 * @throws OperationException
	 *             the operation exception
	 */
	public abstract void initTree(IOperationListener listener) throws OperationException;

	/**
	 * Initialises the tree -- only reads the root's own info but none of the children.
	 *
	 * @throws OperationException
	 *             the operation exception
	 */
	public void initTree() throws OperationException
	{
		initTree(null);
	}

	/**
	 * Builds the file tree by starting from the root and going down.
	 *
	 * @param recursively
	 *            If true, then build all the levels possible under the root.
	 * @throws OperationException
	 *             the operation exception
	 * @throws CreationException
	 */
	public void buildFileTree(boolean recursively) throws OperationException, CreationException
	{
		// make sure there's a root to access.
		if (remoteFileTree == null)
		{
			initTree();
		}

		// build more levels if required.
		remoteFileTree.buildTree(recursively);

		Logger.info("finished building the root tree: " + name);
	}

	/**
	 * Builds the file tree by starting from the root and going down.
	 *
	 * @param numberOfLevels
	 *            How many levels to fetch -- 0 is root level only.
	 * @throws OperationException
	 *             the operation exception
	 */
	public void buildFileTree(int numberOfLevels) throws OperationException
	{
		remoteFileTree.buildTree(numberOfLevels);

		Logger.info("finished building the root tree (" + name + ") up to level: " + numberOfLevels);
	}

	/**
	 * Update existing tree by fetching each sub-folder's children from source. It doesn't 'build' the tree recursively!
	 *
	 * @throws OperationException
	 *             the operation exception
	 */
	public void updateExistingTree() throws OperationException
	{
		remoteFileTree.updateFromSource(true, true);
	}

	/**
	 * Calculate remote free space available on the CSP.
	 *
	 * @return the free space in bytes.
	 * @throws OperationException
	 *             the operation exception
	 */
	public abstract long calculateRemoteFreeSpace() throws OperationException;

	// //////////////////////////////////////////////////////////////////////////////////////
	// #region Transfer.
	// ======================================================================================

	protected void initTransfer(Container<?> container, Container<?> destination, boolean overwrite) throws OperationException
	{
		Logger.info("creating transfer job for " + container.getPath());

		// TODO check the queue as well for files with the same name.
		// overwrite if necessary.
		List<Container<?>> existingContainer = ((Folder<?>) destination).searchByName(container.getName(), false);

		if ( !existingContainer.isEmpty() && (existingContainer.get(0).isFolder() == container.isFolder()))
		{
			if (overwrite)
			{
				existingContainer.get(0).delete();
			}
			else
			{
				throw new OperationException("Already exists!");
			}
		}
	}

	// --------------------------------------------------------------------------------------
	// #region Download.

	/**
	 * Checks the queues, if it's the same job passed, then call cancel;
	 * if not, then simply remove it from the queue.
	 *
	 * @param job
	 *            the job
	 */
	public void cancelTransfer(TransferJob<?> job)
	{
		// just try to remove it from each queue, instead of checking type and such.
		if (currentUploadJob == job)
		{
			cancelCurrentUpload();
		}
		if (currentDownloadJob == job)
		{
			cancelCurrentDownload();
		}
		else
		{
			Logger.info("cancelling transfer: " + job.getSourceFile().getPath());

			uploadQueue.remove(job);
			downloadQueue.remove(job);
		}
	}

	// TODO Test folder download.
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
	 * @return the download jobs
	 * @throws TransferException
	 *             the transfer exception
	 * @throws OperationException
	 *             the operation exception
	 * @throws CreationException
	 *             the creation exception
	 */
	@SuppressWarnings("rawtypes")
	public DownloadJob<?>[] download(RemoteFolder<?> folder, LocalFolder parent, boolean overwrite
			, ITransferProgressListener listener) throws TransferException, OperationException, CreationException
	{
		Logger.info("downloading folder: " + folder.getPath());

		// make sure the folder doesn't exist at the destination.
		List<Container<?>> result = parent.searchByName(folder.getName(), false);
		LocalFolder localFolder = null;

		// if it doesn't exist ...
		if ((result.isEmpty()) || !result.get(0).isFolder())
		{
			// ... create the folder at the destination.
			localFolder = new LocalFolder();
			localFolder.create(parent, event -> {});
			localFolder.updateFromSource(true, false);		// update the folder info
		}
		else
		{	// ... else, just use the one at the destination.
			localFolder = (LocalFolder) result.get(0);
		}

		// link the remote and local folders.
		localFolder.setRemoteMapping(folder);
		folder.setLocalMapping(localFolder);

		// collect the jobs in this array to be returned later.
		List<DownloadJob> downloadJobs = new ArrayList<DownloadJob>();

		// add each file in the folder to the download queue.
		for (com.yagasoft.overcast.base.container.File<?> file : folder.getFilesArray())
		{
			downloadJobs.add(download((RemoteFile) file, parent, overwrite, listener));
		}

		// call the download method for each sub-folder.
		for (Folder<?> childFolder : folder.getFoldersArray())
		{
			downloadJobs.addAll(Arrays.asList(download((RemoteFolder) childFolder, localFolder,
					overwrite, listener)));
		}

		// return the jobs.
		return downloadJobs.toArray(new DownloadJob[downloadJobs.size()]);
	}

	/**
	 * Download the file (passed) from the server.<br />
	 * This method should prepare a job only -- not start the download, and then add the job to the queue.<br />
	 * Please, check the {@link DownloadJob} for what to do here.
	 *
	 * @param file
	 *            File to download.
	 * @param parent
	 *            The local folder to download to. Must pass a {@link LocalFolder} with the path initialised in it.
	 * @param overwrite
	 *            Whether to overwrite existing file on the local disk or not.
	 * @param listener
	 *            Object listening to the changes in the transfer state.
	 * @return the download job
	 * @throws TransferException
	 *             A problem occurred during the transfer of the file.
	 */
	@SuppressWarnings("unchecked")
	public DownloadJob<?> download(RemoteFile<?> file, LocalFolder parent, boolean overwrite,
			ITransferProgressListener listener)
			throws TransferException
	{
		try
		{
			initTransfer(file, parent, overwrite);
			DownloadJob<?> downloadJob = downloadProcess(file, parent, overwrite);
			postDownloadInit(file, (DownloadJob<DownloaderType>) downloadJob, listener);

			return downloadJob;
		}
		catch (OperationException e)
		{
			Logger.error("downloading, can't init transfer");
			Logger.except(e);
			e.printStackTrace();

			throw new TransferException("can't init transfer for " + file.getPath());
		}
	}

	/**
	 * Stuff to do before creating the job. Probably creating a 'LocalFile'.
	 */
	protected void initDownload() throws CreationException
	{}

	/**
	 * Download process. Should create a job, prepare the downloader object, and set the canceller.
	 *
	 * @param file
	 *            File.
	 * @param parent
	 *            Parent.
	 * @param overwrite
	 *            Overwrite.
	 * @param listener
	 *            Listener.
	 * @param remoteFile
	 * @return Download job
	 * @throws TransferException
	 *             the transfer exception
	 */
	protected abstract DownloadJob<DownloaderType> downloadProcess(RemoteFile<?> file, LocalFolder parent, boolean overwrite)
			throws TransferException;

	/**
	 * Post initialisation of download job.
	 *
	 * @param file
	 *            File.
	 * @param downloadJob
	 *            Download job.
	 * @param listener
	 *            Listener.
	 */
	protected void postDownloadInit(RemoteFile<?> file, DownloadJob<DownloaderType> downloadJob,
			ITransferProgressListener listener)
	{
		downloadJob.addProgressListener(listener);
		downloadQueue.add(downloadJob);		// add it to the queue.
		Logger.info("created download job: " + file.getPath());

		nextDownloadJob();		// check if it can be executed immediately.
	}

	/**
	 * If the queue has a job, set it as the current one after removing it from the queue, and then start the job.<br />
	 * <br />
	 * This uses a separate thread so as not to block the program, and to be able to accept more jobs.<br />
	 * This method is automatically called after the job is done.
	 */
	public void nextDownloadJob()
	{
		// if there's nothing being transferred, and there's something in the queue ...
		if ((currentDownloadJob == null) && !downloadQueue.isEmpty())
		{
			// ... take one job from the queue ...
			currentDownloadJob = downloadQueue.remove();

			Logger.info("starting a new download (" + name + "): " + currentDownloadJob.getRemoteFile().getPath());

			currentDownloadThread = new Thread(() ->
			{
				try
				{	// start the transfer (starts when thread starts below).
						initiateDownload();
						Logger.info("finished download (" + name + "): " + currentDownloadJob.getRemoteFile().getPath());
					}
					catch (TransferException e)
					{	// in case of failure, notify the listeners of the failure, and check for more jobs.
						e.printStackTrace();
						currentDownloadJob.failure();
					}
					finally
					{
						resetDownload();
					}
				});

			// go ...
			currentDownloadThread.start();
		}
	}

	/**
	 * Contains the procedure that will actually download the file after its associated thread triggers.<br />
	 * The job's success method should be called here.
	 *
	 * @throws TransferException
	 *             the transfer exception
	 */
	protected abstract void initiateDownload() throws TransferException;

	/**
	 * Cancel current running download.
	 */
	public void cancelCurrentDownload()
	{
		Logger.info("cancelling download: " + currentDownloadJob.getRemoteFile().getPath());

		currentDownloadJob.cancelTransfer();
	}

	/**
	 * Resets the download by removing the one running and adding the next one. It does NOT cancel the current download.
	 */
	public void resetDownload()
	{
		currentDownloadJob = null;
		nextDownloadJob();
	}

	// #endregion Download.
	// --------------------------------------------------------------------------------------

	// --------------------------------------------------------------------------------------
	// #region Upload.

	// TODO Test folder upload.
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
	 * @return the upload jobs
	 * @throws TransferException
	 *             the transfer exception
	 * @throws OperationException
	 *             the operation exception
	 * @throws CreationException
	 *             the creation exception
	 */
	@SuppressWarnings("rawtypes")
	public UploadJob<?, ?>[] upload(LocalFolder folder, RemoteFolder<?> parent, boolean overwrite
			, ITransferProgressListener listener) throws TransferException, OperationException, CreationException
	{
		Logger.info("uploading folder: " + folder.getPath());

		// check if the folder exists at the CSP.
		List<Container<?>> result = parent.searchByName(folder.getName(), false);
		RemoteFolder<?> remoteFolder = null;

		// if it doesn't exist, create it.
		if ((result.isEmpty()) || !result.get(0).isFolder())
		{
			remoteFolder = getAbstractFactory().createFolder();
			remoteFolder.setName(folder.getName());
			remoteFolder.create(parent, event -> {});
//			remoteFolder.updateFromSource(true, false);
		}
		else
		{
			remoteFolder = (RemoteFolder) result.get(0);
		}

		remoteFolder.setLocalMapping(folder);
		folder.setRemoteMapping(remoteFolder);

		ArrayList<UploadJob> uploadJobs = new ArrayList<UploadJob>();

		// go through the files in the folder, and create an upload job for them.
		for (com.yagasoft.overcast.base.container.File<?> file : folder.getFilesArray())
		{
			uploadJobs.add(upload((LocalFile) file, parent, overwrite, listener));
		}

		// check sub-folders as well.
		for (Folder childFolder : folder.getFoldersArray())
		{
			uploadJobs.addAll(Arrays.asList(upload((LocalFolder) childFolder, remoteFolder,
					overwrite, listener)));
		}

		return uploadJobs.toArray(new UploadJob[uploadJobs.size()]);
	}

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
	 * @return the upload job
	 * @throws TransferException
	 *             A problem occurred during the transfer of the file.
	 */
	@SuppressWarnings("unchecked")
	public UploadJob<?, ?> upload(LocalFile file, RemoteFolder<?> parent, boolean overwrite,
			ITransferProgressListener listener)
			throws TransferException
	{
		try
		{
			initTransfer(file, parent, overwrite);
			UploadJob<?, ?> uploadJob = uploadProcess(file, parent, overwrite, initUpload());
			postUploadInit(file, (UploadJob<UploaderType, SourceFileType>) uploadJob, listener);

			return uploadJob;
		}
		catch (OperationException | CreationException e)
		{
			Logger.error("uploading, can't init transfer");
			Logger.except(e);
			e.printStackTrace();

			throw new TransferException("can't init transfer for " + file.getPath());
		}
	}

	/**
	 * Stuff to do before creating the job. Probably creating a 'RemoteFile'.
	 *
	 * @return Remote file
	 * @throws CreationException
	 *             the creation exception
	 */
	protected RemoteFile<?> initUpload() throws CreationException
	{
		// create an object for the file that's going to be uploaded to be linked to.
		return getAbstractFactory().createFile();
	}

	/**
	 * Upload process. Should create a job, prepare the uploader object, and set the canceller.
	 *
	 * @param file
	 *            File.
	 * @param parent
	 *            Parent.
	 * @param overwrite
	 *            Overwrite.
	 * @param listener
	 *            Listener.
	 * @param remoteFile
	 * @return Upload job
	 * @throws TransferException
	 *             the transfer exception
	 */
	protected abstract UploadJob<UploaderType, SourceFileType> uploadProcess(LocalFile file, RemoteFolder<?> parent,
			boolean overwrite,
			RemoteFile<?> remoteFile)
			throws TransferException;

	/**
	 * Post initialisation of upload job.
	 *
	 * @param file
	 *            File.
	 * @param uploadJob
	 *            Upload job.
	 * @param listener
	 *            Listener.
	 */
	protected void postUploadInit(LocalFile file, UploadJob<UploaderType, SourceFileType> uploadJob,
			ITransferProgressListener listener)
	{
		uploadJob.addProgressListener(listener);
		uploadQueue.add(uploadJob);		// add it to the queue.
		Logger.info("created upload job: " + file.getPath());

		nextUploadJob();		// check if it can be executed immediately.
	}

	/**
	 * If the queue has a job, set it as the current one after removing it from the queue, and then start the job. <br />
	 * This uses a separate thread so as not to block the program.
	 */
	public void nextUploadJob()
	{
		// if no transfers, and queue has a job ...
		if ((currentUploadJob == null) && !uploadQueue.isEmpty())
		{
			currentUploadJob = uploadQueue.remove();

			Logger.info("starting a new upload (" + name + "): " + currentUploadJob.getLocalFile().getPath());

			currentUploadThread = new Thread(() ->
			{
				try
				{
					// start the transfer (starts when thread starts below).
					initiateUpload();

					Logger.info("finished upload (" + name + "): " + currentUploadJob.getLocalFile().getPath());
				}
				catch (TransferException e)
				{
					e.printStackTrace();
					currentUploadJob.failure();
				}
				finally
				{
					resetUpload();
				}
			});

			// go ...
			currentUploadThread.start();
		}
	}

	/**
	 * Starts the upload using the 'cspTransferer' included in the job,
	 * and passes the result to the upload job using 'success' method to add to the file object.
	 *
	 * @throws TransferException
	 *             the transfer exception
	 */
	protected abstract void initiateUpload() throws TransferException;

	/**
	 * Cancel current upload.
	 */
	public void cancelCurrentUpload()
	{
		Logger.info("cancelling upload: " + currentUploadJob.getLocalFile().getPath());

		currentUploadJob.cancelTransfer();
	}

	/**
	 * Resets the upload by removing the one running and adding the next one. It does NOT cancel the current upload.
	 */
	public void resetUpload()
	{
		currentUploadJob = null;
		nextUploadJob();
	}

	// #endregion Upload.
	// --------------------------------------------------------------------------------------

	// ======================================================================================
	// #endregion Transfer.
	// //////////////////////////////////////////////////////////////////////////////////////

	// //////////////////////////////////////////////////////////////////////////////////////
	// #region Search by path.
	// ======================================================================================

	/**
	 * Search for folder by path.
	 *
	 * @param path
	 *            Path.
	 * @return Remote folder
	 * @throws OperationException
	 *             the operation exception
	 */
	public RemoteFolder<?> searchFolderByPath(String path) throws OperationException
	{
		Container<?> result = searchContainerPath(path);

		// if nothing is found, or the result has a type doesn't match with the one required ...
		if ((result != null) && result.isFolder())
		{
			return (RemoteFolder<?>) result;		// ... then result found.
		}
		else
		{
			return null;			// ... else, return nothing.
		}
	}

	/**
	 * Search for file by path.
	 *
	 * @param path
	 *            Path.
	 * @return Remote file
	 * @throws OperationException
	 *             the operation exception
	 */
	public RemoteFile<?> searchFileByPath(String path) throws OperationException
	{
		Container<?> result = searchContainerPath(path);

		// if nothing is found, or the result has a type doesn't match with the one required ...
		if ((result != null) && !result.isFolder())
		{
			return (RemoteFile<?>) result;		// ... then result found.
		}
		else
		{
			return null;			// ... else, return nothing.
		}
	}

	/**
	 * Search for the container in the file tree online using the path passed.
	 *
	 * @param path
	 *            the path to look through for the container.
	 * @return the container found, or null if nothing is found.
	 * @throws OperationException
	 */
	public Container<?> searchContainerPath(String path) throws OperationException
	{
		ArrayList<String> splitPath = splitPath(path);
		// get the name from the last entry in the path.
		String containerName = splitPath.remove(splitPath.size() - 1);

		// save intermediate nodes
		List<Container<?>> result = new ArrayList<Container<?>>();
		result.add(remoteFileTree);

		// search for each entry in the path ...
		while ( !result.isEmpty() && !splitPath.isEmpty())
		{
			if ( !result.get(0).isFolder())
			{	// found a file in the middle of the path -- not what we're looking for.
				Logger.error("failed to search " + path);
				throw new OperationException("Couldn't complete search: " + path);
			}
			else
			{	// search for the next node in this node.
				result = ((RemoteFolder<?>) result.get(0)).searchByName(splitPath.remove(0), false);
			}
		}

		// if part of the path is not found ...
		if ( !splitPath.isEmpty() || (result.isEmpty()))
		{
			return null;		// ... return nothing.
		}
		else
		{	// ... or search for the file in the end node. Might return null.
			return ((RemoteFolder<?>) result.get(0)).searchByName(containerName, false).stream().findFirst().orElse(null);
		}

	}

	// ======================================================================================
	// #endregion Search by path.
	// //////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Split the path into individual nodes.
	 *
	 * @param path
	 *            the path
	 * @return the array list of nodes in the path
	 */
	public ArrayList<String> splitPath(String path)
	{
		ArrayList<String> splitPath = new ArrayList<String>(Arrays.asList(path.split("/")));

		// if the path starts with '/' it will cause the first entry to be empty!
		if (splitPath.get(0).equals(""))
		{
			splitPath.remove(0);
		}

		return splitPath;
	}

	/**
	 * Returns the factory as an abstract one -- can't predict its type from here.
	 *
	 * @return the abstract factory
	 */
	public abstract RemoteFactory<?, ?, ?, ?, ?> getAbstractFactory();

	/**
	 * Returns the name of the CSP.
	 */
	@Override
	public String toString()
	{
		return name;
	}

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
