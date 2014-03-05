
package com.yagasoft.overcast.google;


import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.Arrays;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.googleapis.media.MediaHttpUploaderProgressListener;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.ParentReference;
import com.yagasoft.overcast.CSP;
import com.yagasoft.overcast.UploadJob;
import com.yagasoft.overcast.container.Container;
import com.yagasoft.overcast.container.Folder;
import com.yagasoft.overcast.container.ITransferProgressListener;
import com.yagasoft.overcast.container.ITransferProgressListener.TransferState;
import com.yagasoft.overcast.container.LocalFile;
import com.yagasoft.overcast.container.LocalFolder;


public class Google extends CSP implements MediaHttpUploaderProgressListener
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
			driveService = new Drive.Builder(httpTransport, JSON_FACTORY, authorisation.credential).setApplicationName(
					APPLICATION_NAME).build();

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
	 * @see com.yagasoft.overcast.CSP#upload(com.yagasoft.overcast.container.LocalFile,
	 *      com.yagasoft.overcast.container.RemoteFolder, boolean, com.yagasoft.overcast.container.ITransferProgressListener,
	 *      java.lang.Object)
	 */
	@Override
	public void upload(LocalFile file, com.yagasoft.overcast.container.RemoteFolder<?> parent, boolean overwrite,
			ITransferProgressListener listener,
			Object object) throws Exception
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
					throw new Exception("File exists!");
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

			UploadJob<Drive.Files.Insert> uploadJob = new UploadJob<Drive.Files.Insert>(file, parent, overwrite, insert);
			uploadQueue.add(uploadJob);

			nextUploadJob();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * @see com.yagasoft.overcast.CSP#upload(com.yagasoft.overcast.container.LocalFolder,
	 *      com.yagasoft.overcast.container.RemoteFolder, boolean, com.yagasoft.overcast.container.ITransferProgressListener,
	 *      java.lang.Object)
	 */
	@Override
	public void upload(LocalFolder folder, com.yagasoft.overcast.container.RemoteFolder<?> parent, boolean overwrite,
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
			catch (Exception e)
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
			catch (Exception e)
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
	 * @see com.yagasoft.overcast.CSP#nextUploadJob()
	 */
	@Override
	public void nextUploadJob()
	{
		if (currentUploadJob == null)
		{
			currentUploadJob = uploadQueue.remove();

			try
			{
				currentUploadJob.getParent().add(
						factory.createFile(((Drive.Files.Insert) currentUploadJob.getCspUploader()).execute(), false));
			}
			catch (IOException e)
			{
				e.printStackTrace();
				currentUploadJob.getFile().notifyListeners(TransferState.FAILED, 0f);
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
				currentUploadJob.getFile().notifyListeners(TransferState.INITIALISED, (float) uploader.getProgress());

			case MEDIA_IN_PROGRESS:
				// System.out.println("Progress: " +
				// NumberFormat.getPercentInstance().format(uploader.getProgress()));
				currentUploadJob.getFile().notifyListeners(TransferState.IN_PROGRESS, (float) uploader.getProgress());
				break;

			case MEDIA_COMPLETE:
				currentUploadJob.getFile().notifyListeners(TransferState.COMPLETED, (float) uploader.getProgress());
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
	 * @param factory the factory to set
	 */
	public static void setFactory(RemoteFactory factory)
	{
		Google.factory = factory;
	}

	// ======================================================================================
	// #endregion Getters and setters.
	// //////////////////////////////////////////////////////////////////////////////////////

}
