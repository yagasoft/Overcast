/*
 * Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
 *
 *		Modified MIT License (GPL v3 compatible)
 * 			License terms are in a separate file (license.txt)
 *
 *		Project/File: Overcast/com.yagasoft.overcast.google/Google.java
 *
 *			Modified: 13-Mar-2014 (18:22:27)
 *			   Using: Eclipse J-EE / JDK 7 / Windows 8.1 x64
 */

package com.yagasoft.overcast.google;


import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.media.MediaHttpDownloader;
import com.google.api.client.googleapis.media.MediaHttpDownloaderProgressListener;
import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.googleapis.media.MediaHttpUploaderProgressListener;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.About;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.ParentReference;
import com.yagasoft.overcast.CSP;
import com.yagasoft.overcast.container.Container;
import com.yagasoft.overcast.container.Folder;
import com.yagasoft.overcast.container.local.LocalFile;
import com.yagasoft.overcast.container.local.LocalFolder;
import com.yagasoft.overcast.container.transfer.DownloadJob;
import com.yagasoft.overcast.container.transfer.ITransferProgressListener;
import com.yagasoft.overcast.container.transfer.TransferState;
import com.yagasoft.overcast.container.transfer.UploadJob;
import com.yagasoft.overcast.exception.CreationException;
import com.yagasoft.overcast.exception.OperationException;
import com.yagasoft.overcast.exception.TransferException;


/**
 * Class representing Google Drive. It handles authentication, transfer of files, and contains the root.
 */
public class Google extends CSP<File, MediaHttpDownloader, Drive.Files.Insert> implements MediaHttpDownloaderProgressListener,
		MediaHttpUploaderProgressListener
{

	/**
	 * Be sure to specify the name of your application. If the application name is {@code null} or blank, the application will log
	 * a warning.
	 * Suggested format is "MyCompany-ProductName/1.0".
	 */
	static final String			APPLICATION_NAME	= "Overcast";

	/** Global instance of the HTTP transport. */
	static HttpTransport		httpTransport;

	/** Global Drive API client. */
	static Drive				driveService;

	/** Global instance of the JSON factory. */
	static final JsonFactory	JSON_FACTORY		= JacksonFactory.getDefaultInstance();

	/** The authorisation. */
	Authorisation				authorisation;

	/** The remote file factory. */
	static RemoteFactory		factory;

	/**
	 * Instantiates a new google.
	 */
	public Google()
	{
		try
		{
			// used in authorisation and transfers.
			httpTransport = GoogleNetHttpTransport.newTrustedTransport();

			// authenticate.
			authorisation = new Authorisation();
			authorisation.setInfo(Paths.get(Authorisation.class.getResource("/client_secrets.json").toURI()));
			authorisation.authorise();

			// set up the global Drive instance
			driveService = new Drive.Builder(httpTransport, JSON_FACTORY, authorisation.credential)
					.setApplicationName(APPLICATION_NAME).build();

			// initialise the remote file factory.
			factory = new RemoteFactory(this);

			remoteFreeSpace = calculateRemoteFreeSpace();

			name = "Google Drive";
		}
		catch (IOException | GeneralSecurityException | URISyntaxException | OperationException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * @see com.yagasoft.overcast.CSP#initTree()
	 */
	@Override
	public void initTree()
	{
		remoteFileTree = factory.createFolder();
		remoteFileTree.setId("root");
		remoteFileTree.updateFromSource();
		buildFileTree(false);
	}

//	/**
//	 * @see com.yagasoft.overcast.CSP#buildFileTree(boolean)
//	 */
//	@Override
//	public void buildFileTree(boolean recursively)
//	{
//		remoteFileTree.buildTree(recursively);
//
////		for (Container<?> container : root.getChildrenList())
////		{
////			System.out.println(container.getName());
////		}
//
////		try
////		{
////		RemoteFolder root = new RemoteFolder(getDriveService());
////		root.setApiFile(new File());
////		root.setId("root");
////		root.update(recursive);
////
////		setFullTreeLoaded(recursive);
////
////		setRemoteFileTree(root);
//
////			Iterator<TreeNode> iterator = getRemoteFileTree().preorderIterator();
////
////			while (iterator.hasNext())
////			{
////				File file = driveService.files().get(((IRemote) iterator.next()).getId()).execute();
////				System.out.println("Title: " + file.getTitle());
////				System.out.println("Description: " + file.getDescription());
////				System.out.println("MIME type: " + file.getMimeType());
////				System.out.println("\n");
////			}
////		}
////		catch (IOException e)
////		{
////			e.printStackTrace();
////		}
//	}

	/**
	 * @see com.yagasoft.overcast.CSP#calculateRemoteFreeSpace()
	 */
	@Override
	public long calculateRemoteFreeSpace() throws OperationException
	{
		About about;
		try
		{
			about = driveService.about().get().execute();
			remoteFreeSpace = about.getQuotaBytesTotal() - about.getQuotaBytesUsed();

			return remoteFreeSpace;
		}
		catch (IOException e)
		{
			e.printStackTrace();
			throw new OperationException("Couldn't get free space.");
		}
	}

	/**
	 * @see com.yagasoft.overcast.CSP#download(com.yagasoft.overcast.container.remote.RemoteFolder,
	 *      com.yagasoft.overcast.container.local.LocalFolder, boolean,
	 *      com.yagasoft.overcast.container.transfer.ITransferProgressListener, java.lang.Object)
	 */
	@Override
	public DownloadJob<?>[] download(com.yagasoft.overcast.container.remote.RemoteFolder<?> folder, LocalFolder parent
			, boolean overwrite, ITransferProgressListener listener, Object object)
	{
		// make sure the folder doesn't exist at the destination.
		Container<?> result = parent.searchByName(folder.getName(), false);
		LocalFolder localFolder = null;

		// if it doesn't exist ...
		if ((result == null) || !result.isFolder())
		{
			// ... create the folder at the destination.
			localFolder = new LocalFolder();
			localFolder.create(parent);
			localFolder.updateFromSource(true, false);
		}
		else
		{	// ... else, just use the one at the destination.
			localFolder = (LocalFolder) result;
		}

		localFolder.setRemoteMapping(folder);
		folder.setLocalMapping(localFolder);

		ArrayList<DownloadJob<?>> downloadJobs = new ArrayList<DownloadJob<?>>();

		// add each file in the folder to the download queue.
		for (com.yagasoft.overcast.container.File<?> file : folder.getFilesArray())
		{
			try
			{
				downloadJobs.add(((RemoteFile) file).download(parent, overwrite, listener, object));
			}
			catch (TransferException | OperationException e)
			{
				e.printStackTrace();
			}
		}

		// call the download method for each sub-folder.
		for (Folder<?> childFolder : folder.getFoldersArray())
		{
			try
			{
				downloadJobs.addAll(new ArrayList<DownloadJob<?>>(Arrays.asList(((RemoteFolder) childFolder).download(
						localFolder, overwrite,
						listener, object))));
			}
			catch (TransferException e)
			{
				e.printStackTrace();
			}
		}

		return downloadJobs.toArray(new DownloadJob<?>[downloadJobs.size()]);
	}

	/**
	 * @see com.yagasoft.overcast.CSP#download(com.yagasoft.overcast.container.remote.RemoteFile,
	 *      com.yagasoft.overcast.container.local.LocalFolder, boolean,
	 *      com.yagasoft.overcast.container.transfer.ITransferProgressListener, java.lang.Object)
	 */
	@Override
	public DownloadJob<?> download(com.yagasoft.overcast.container.remote.RemoteFile<?> file, LocalFolder parent
			, boolean overwrite, ITransferProgressListener listener, Object object) throws TransferException, OperationException
	{
		// check for the file existence in the parent
		for (com.yagasoft.overcast.container.File<?> child : parent.getFilesArray())
		{
			// if it exists ...
			if (file.getName().equals(child.getName()))
			{
				// ... delete if required.
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

		// initialise downloader.
		MediaHttpDownloader downloader = new MediaHttpDownloader(Google.getHttpTransport()
				, Google.driveService.getRequestFactory().getInitializer());
		downloader.setDirectDownloadEnabled(false);
		downloader.setProgressListener(this);
		downloader.setChunkSize(MediaHttpUploader.MINIMUM_CHUNK_SIZE);

		// add the transfer listener to the file object.
		file.addProgressListener(listener, object);

		// create a download job and add it to the queue.
		DownloadJob<MediaHttpDownloader> downloadJob = new DownloadJob<MediaHttpDownloader>(file, parent, overwrite
				, downloader);
		downloadQueue.add(downloadJob);

		nextDownloadJob();		// check if this job can be executed right away.

		return downloadJob;
	}

	/**
	 * @see com.yagasoft.overcast.CSP#nextDownloadJob()
	 */
	@Override
	public void nextDownloadJob()
	{
		new Thread(new Runnable()
		{

			@Override
			public void run()
			{
				// if there's nothing being transferred, and there's something in the queue ...
				if ((currentDownloadJob == null) && !downloadQueue.isEmpty())
				{
					// ... take one job from the queue ...
					currentDownloadJob = downloadQueue.remove();

					try
					{
						// ... get a stream to the file on the local disk, and start the download.
						OutputStream out = new FileOutputStream(currentDownloadJob.getLocalFile().getSourceObject().toFile());
						currentDownloadJob.getCspTransferer().download(
								new GenericUrl(currentDownloadJob.getRemoteFile().getLink()), out);
						out.close();		// close the file stream after download has finished.
					}
					catch (IOException e)
					{	// in case of failure, notify the listeners of the failure, and check for more jobs.
						e.printStackTrace();
						currentDownloadJob.failure();
						nextDownloadJob();
					}
				}
			}
		}).start();
	}

	/**
	 * @see com.google.api.client.googleapis.media.MediaHttpDownloaderProgressListener#progressChanged(com.google.api.client.googleapis.media.MediaHttpDownloader)
	 */
	@Override
	public void progressChanged(MediaHttpDownloader downloader) throws IOException
	{
		switch (downloader.getDownloadState())
		{
			case MEDIA_IN_PROGRESS:
				// System.out.println("Progress: " +
				// NumberFormat.getPercentInstance().format(downloader.getProgress()));
				currentDownloadJob.progress((float) downloader.getProgress());		// notify listeners of progress.
				break;

			case MEDIA_COMPLETE:
				// download finished successfully; notify listeners.
				currentDownloadJob.success();
				currentDownloadJob = null;		// clear current job.
				nextDownloadJob();		// check if there're more jobs.
				break;

			default:
				System.out.println(downloader.getDownloadState());
				break;
		}

	}

	/**
	 * @see com.yagasoft.overcast.CSP#upload(com.yagasoft.overcast.container.local.LocalFolder,
	 *      com.yagasoft.overcast.container.remote.RemoteFolder, boolean,
	 *      com.yagasoft.overcast.container.transfer.ITransferProgressListener, java.lang.Object)
	 */
	@Override
	public UploadJob<?, ?>[] upload(LocalFolder folder, com.yagasoft.overcast.container.remote.RemoteFolder<?> parent
			, boolean overwrite, ITransferProgressListener listener, Object object)
	{
		// check if the folder exists at the CSP.
		Container<?> result = parent.searchByName(folder.getName(), false);
		RemoteFolder remoteFolder = null;

		// if it doesn't exist, create it.
		if ((result == null) || !result.isFolder())
		{
			try
			{
				remoteFolder = factory.createFolder();
				remoteFolder.setName(folder.getName());
				remoteFolder.create(parent);
				remoteFolder.updateFromSource(true, false);
			}
			catch (CreationException e)
			{
				e.printStackTrace();
			}
		}
		else
		{
			remoteFolder = (RemoteFolder) result;
		}

		remoteFolder.setLocalMapping(folder);
		folder.setRemoteMapping(remoteFolder);

		ArrayList<UploadJob<?, ?>> uploadJobs = new ArrayList<UploadJob<?, ?>>();

		// go through the files in the folder, and create an upload job for them.
		for (com.yagasoft.overcast.container.File<?> file : folder.getFilesArray())
		{
			try
			{
				uploadJobs.add(upload((LocalFile) file, parent, overwrite, listener, object));
			}
			catch (TransferException | OperationException e)
			{
				e.printStackTrace();
			}
		}

		// check sub-folders as well.
		for (Folder<?> childFolder : folder.getFoldersArray())
		{
			uploadJobs.addAll(new ArrayList<UploadJob<?, ?>>(Arrays.asList(upload((LocalFolder) childFolder, remoteFolder,
					overwrite, listener,
					object))));
		}

		return uploadJobs.toArray(new UploadJob<?, ?>[uploadJobs.size()]);
	}

	/**
	 * @see com.yagasoft.overcast.CSP#upload(com.yagasoft.overcast.container.local.LocalFile,
	 *      com.yagasoft.overcast.container.remote.RemoteFolder, boolean,
	 *      com.yagasoft.overcast.container.transfer.ITransferProgressListener, java.lang.Object)
	 */
	@Override
	public UploadJob<?, ?> upload(LocalFile file, com.yagasoft.overcast.container.remote.RemoteFolder<?> parent
			, boolean overwrite, ITransferProgressListener listener, Object object) throws TransferException, OperationException
	{
		// overwrite if necessary.
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

		// prepare file information to be passed to Google service.
		File metadata = new File();
		metadata.setTitle(file.getName());
		metadata.setMimeType(file.getType());
		metadata.setParents(Arrays.asList(new ParentReference().setId(parent.getId())));

		// file content object to be passed as well.
		FileContent content = new FileContent(file.getType(), file.getSourceObject().toFile());

		try
		{
			// pass info and content object, and create the uploader.
			Drive.Files.Insert insert = Google.driveService.files().insert(metadata, content);

			// initialise the uploader.
			MediaHttpUploader uploader = insert.getMediaHttpUploader();
			uploader.setDirectUploadEnabled(false);
			uploader.setProgressListener(this);
			uploader.setChunkSize(MediaHttpUploader.MINIMUM_CHUNK_SIZE);

			file.addProgressListener(listener, object);

			// create an object for the file that's going to be uploaded to be linked to.
			RemoteFile remoteFile = factory.createFile();

			// create an upload job.
			UploadJob<Drive.Files.Insert, File> uploadJob = new UploadJob<Drive.Files.Insert, File>(file, remoteFile, parent
					, overwrite, insert);
			uploadQueue.add(uploadJob);		// add it to the queue.

			nextUploadJob();		// check if it can be executed immediately.

			return uploadJob;
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * @see com.yagasoft.overcast.CSP#nextUploadJob()
	 */
	@Override
	public void nextUploadJob()
	{
		new Thread(new Runnable()
		{

			@Override
			public void run()
			{
				// if no transfers, and queue has a job ...
				if ((currentUploadJob == null) && !uploadQueue.isEmpty())
				{
					currentUploadJob = uploadQueue.remove();

					try
					{
						// start the transfer and pass the Google file to the upload job to add to the file object.
						currentUploadJob.success(currentUploadJob.getCspTransferer().execute());
					}
					catch (IOException e)
					{
						e.printStackTrace();
						currentUploadJob.failure();
						nextUploadJob();
					}
				}
			}
		}).start();
	}

	/**
	 * @see com.google.api.client.googleapis.media.MediaHttpUploaderProgressListener#progressChanged(com.google.api.client.googleapis.media.MediaHttpUploader)
	 */
	@Override
	public void progressChanged(MediaHttpUploader uploader) throws IOException
	{
		switch (uploader.getUploadState())
		{
			case INITIATION_COMPLETE:
				currentUploadJob.notifyListeners(TransferState.INITIALISED, 0.0f);

			case MEDIA_IN_PROGRESS:
				// System.out.println("Progress: " +
				// NumberFormat.getPercentInstance().format(uploader.getProgress()));
				currentUploadJob.progress((float) uploader.getProgress());
				break;

			case MEDIA_COMPLETE:
				currentUploadJob.notifyListeners(TransferState.COMPLETED, 1.0f);
				currentUploadJob = null;
				nextUploadJob();
				break;

			default:
				System.out.println(uploader.getUploadState());
				break;
		}
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
		Google.factory = factory;
	}

	// ======================================================================================
	// #endregion Getters and setters.
	// //////////////////////////////////////////////////////////////////////////////////////

}
