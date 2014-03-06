
package com.yagasoft.overcast.google;


import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
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
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.ParentReference;
import com.yagasoft.overcast.CSP;
import com.yagasoft.overcast.container.Container;
import com.yagasoft.overcast.container.Folder;
import com.yagasoft.overcast.container.local.LocalFile;
import com.yagasoft.overcast.container.local.LocalFolder;
import com.yagasoft.overcast.container.remote.IRemote;
import com.yagasoft.overcast.container.remote.RemoteFile;
import com.yagasoft.overcast.container.transfer.DownloadJob;
import com.yagasoft.overcast.container.transfer.ITransferProgressListener;
import com.yagasoft.overcast.container.transfer.ITransferProgressListener.TransferState;
import com.yagasoft.overcast.container.transfer.UploadJob;
import com.yagasoft.overcast.exception.CreationException;
import com.yagasoft.overcast.exception.TransferException;


public class Google extends CSP<File, MediaHttpDownloader, Drive.Files.Insert> implements MediaHttpDownloaderProgressListener,
		MediaHttpUploaderProgressListener
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
	
	static RemoteFactory		factory;
	
	public Google()
	{
		try
		{
			httpTransport = GoogleNetHttpTransport.newTrustedTransport();
			
			authorisation = new Authorisation();
			authorisation.setInfo(Paths.get(Authorisation.class.getResource("/client_secrets.json").toURI()));
			authorisation.authorise();
			
			// set up the global Drive instance
			driveService = new Drive.Builder(httpTransport, JSON_FACTORY, authorisation.credential)
					.setApplicationName(APPLICATION_NAME).build();
			
			factory = new RemoteFactory(this);
		}
		catch (IOException | GeneralSecurityException | URISyntaxException e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * @see com.yagasoft.overcast.CSP#initTree()
	 */
	@Override
	public void initTree()
	{}
	
	/**
	 * @see com.yagasoft.overcast.CSP#buildFileTree(boolean)
	 */
	@Override
	public void buildFileTree(boolean recursively)
	{
		remoteFileTree = factory.createFolder();
		remoteFileTree.setId("root");
		remoteFileTree.updateFromSource(false, false);
		remoteFileTree.buildTree(recursively ? Integer.MAX_VALUE : 1);
		
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
	
	// //////////////////////////////////////////////////////////////////////////////////////
	// #region Getters and setters.
	// ======================================================================================
	
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
	
	/**
	 * @see com.yagasoft.overcast.CSP#download(com.yagasoft.overcast.container.remote.RemoteFolder,
	 *      com.yagasoft.overcast.container.local.LocalFolder, boolean,
	 *      com.yagasoft.overcast.container.transfer.ITransferProgressListener, java.lang.Object)
	 */
	@Override
	public void download(com.yagasoft.overcast.container.remote.RemoteFolder<?> folder, LocalFolder parent, boolean overwrite,
			ITransferProgressListener listener, Object object)
	{
		Container<?> result = parent.searchByName(folder.getName(), false);
		LocalFolder localFolder = null;
		
		if ((result == null) || !result.isFolder())
		{
			localFolder = new LocalFolder();
			localFolder.create(parent);
			localFolder.updateFromSource(true, false);
		}
		else
		{
			localFolder = (LocalFolder) result;
		}
		
		localFolder.setRemoteMapping(folder);
		folder.setLocalMapping(localFolder);
		
		for (com.yagasoft.overcast.container.File<?> file : folder.getFilesArray())
		{
			try
			{
				((IRemote) file).download(parent, overwrite, listener, object);
			}
			catch (TransferException e)
			{
				e.printStackTrace();
			}
		}
		
		for (Folder<?> childFolder : folder.getFoldersArray())
		{
			try
			{
				((IRemote) childFolder).download(localFolder, overwrite, listener, object);
			}
			catch (TransferException e)
			{
				e.printStackTrace();
			}
		}
		
	}
	
	/**
	 * @see com.yagasoft.overcast.CSP#download(com.yagasoft.overcast.container.remote.RemoteFile,
	 *      com.yagasoft.overcast.container.local.LocalFolder, boolean,
	 *      com.yagasoft.overcast.container.transfer.ITransferProgressListener, java.lang.Object)
	 */
	@Override
	public void download(RemoteFile<File> file, LocalFolder parent, boolean overwrite, ITransferProgressListener listener,
			Object object) throws TransferException
	{
		for (com.yagasoft.overcast.container.File<?> child : parent.getFilesArray())
		{
			if (child.getName().equals(child.getName()))
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
		
		MediaHttpDownloader downloader = new MediaHttpDownloader(Google.getHttpTransport()
				, Google.driveService.getRequestFactory().getInitializer());
		downloader.setDirectDownloadEnabled(false);
		downloader.setProgressListener(this);
		downloader.setChunkSize(MediaHttpUploader.MINIMUM_CHUNK_SIZE);
		
		file.addProgressListener(listener, object);
		
		DownloadJob<MediaHttpDownloader> downloadJob = new DownloadJob<MediaHttpDownloader>(file, parent, overwrite
				, downloader);
		downloadQueue.add(downloadJob);
		
		nextDownloadJob();
	}
	
	/**
	 * @see com.yagasoft.overcast.CSP#nextDownloadJob()
	 */
	@Override
	public void nextDownloadJob()
	{
		if ((currentDownloadJob == null) && !downloadQueue.isEmpty())
		{
			currentDownloadJob = downloadQueue.remove();
			
			try
			{
				OutputStream out = new FileOutputStream(currentDownloadJob.getLocalFile().getSourceObject().toFile());
				currentDownloadJob.getCspTransferer().download(new GenericUrl(currentDownloadJob.getRemoteFile().getLink()), out);
				out.close();
				
				currentDownloadJob.success();
			}
			catch (IOException e)
			{
				e.printStackTrace();
				currentDownloadJob.notifyListeners(TransferState.FAILED, 0.0f);
				nextDownloadJob();
			}
		}
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
				currentDownloadJob.notifyListeners(TransferState.IN_PROGRESS, (float) downloader.getProgress());
				break;
			
			case MEDIA_COMPLETE:
				currentDownloadJob.notifyListeners(TransferState.COMPLETED, (float) downloader.getProgress());
				currentDownloadJob = null;
				nextDownloadJob();
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
	public void upload(LocalFolder folder, com.yagasoft.overcast.container.remote.RemoteFolder<?> parent, boolean overwrite,
			ITransferProgressListener listener, Object object)
	{
		Container<?> result = parent.searchByName(folder.getName(), false);
		RemoteFolder remoteFolder = null;
		
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
		
		for (com.yagasoft.overcast.container.File<?> file : folder.getFilesArray())
		{
			try
			{
				upload((LocalFile) file, parent, overwrite, listener, object);
			}
			catch (TransferException e)
			{
				e.printStackTrace();
			}
		}
		
		for (Folder<?> childFolder : folder.getFoldersArray())
		{
			upload((LocalFolder) childFolder, remoteFolder, overwrite, listener, object);
		}
	}
	
	/**
	 * @see com.yagasoft.overcast.CSP#upload(com.yagasoft.overcast.container.local.LocalFile,
	 *      com.yagasoft.overcast.container.remote.RemoteFolder, boolean,
	 *      com.yagasoft.overcast.container.transfer.ITransferProgressListener, java.lang.Object)
	 */
	@Override
	public void upload(LocalFile file, com.yagasoft.overcast.container.remote.RemoteFolder<?> parent, boolean overwrite,
			ITransferProgressListener listener,
			Object object) throws TransferException
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
		
		File metadata = new File();
		metadata.setTitle(file.getName());
		metadata.setMimeType(file.getType());
		metadata.setParents(Arrays.asList(new ParentReference().setId(parent.getId())));
		
		FileContent content = new FileContent(file.getType(), file.getSourceObject().toFile());
		
		try
		{
			Drive.Files.Insert insert = Google.driveService.files().insert(metadata, content);
			
			MediaHttpUploader uploader = insert.getMediaHttpUploader();
			uploader.setDirectUploadEnabled(false);
			uploader.setProgressListener(this);
			uploader.setChunkSize(MediaHttpUploader.MINIMUM_CHUNK_SIZE);
			
			file.addProgressListener(listener, object);
			
			RemoteFile<File> remoteFile = factory.createFile();
			
			UploadJob<Drive.Files.Insert, File> uploadJob = new UploadJob<Drive.Files.Insert, File>(file, remoteFile, parent
					, overwrite, insert);
			uploadQueue.add(uploadJob);
			
			nextUploadJob();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * @see com.yagasoft.overcast.CSP#nextUploadJob()
	 */
	@Override
	public void nextUploadJob()
	{
		if ((currentUploadJob == null) && !uploadQueue.isEmpty())
		{
			currentUploadJob = uploadQueue.remove();
			
			try
			{
				currentUploadJob.success(currentUploadJob.getCspTransferer().execute());
			}
			catch (IOException e)
			{
				e.printStackTrace();
				currentUploadJob.notifyListeners(TransferState.FAILED, 0.0f);
				nextUploadJob();
			}
		}
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
				currentUploadJob.notifyListeners(TransferState.IN_PROGRESS, (float) uploader.getProgress());
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
