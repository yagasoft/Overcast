
package com.yagasoft.overcast.google;


import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

import com.google.api.client.googleapis.media.MediaHttpDownloader;
import com.google.api.client.googleapis.media.MediaHttpDownloaderProgressListener;
import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.http.GenericUrl;
import com.google.api.services.drive.model.File;
import com.yagasoft.overcast.container.Container;
import com.yagasoft.overcast.container.Folder;
import com.yagasoft.overcast.container.IRemote;
import com.yagasoft.overcast.container.ITransferProgressListener;
import com.yagasoft.overcast.container.ITransferProgressListener.TransferState;


public class RemoteFile extends com.yagasoft.overcast.container.RemoteFile<File> implements IRemote, MediaHttpDownloaderProgressListener
{
	
	/**
	 * Better use the factory in Google class.
	 */
	public RemoteFile()
	{}
	
	/**
	 * @see com.yagasoft.overcast.container.Container#isExist()
	 */
	@Override
	public boolean isExist()
	{
		return false;
	}
	
	/**
	 * @see com.yagasoft.overcast.container.Container#updateInfo()
	 */
	@Override
	public void updateInfo()
	{
		id = sourceObject.getId();
		name = sourceObject.getTitle();
		type = sourceObject.getMimeType();
		
		try
		{
			link = new URL(sourceObject.getDownloadUrl());
		}
		catch (MalformedURLException e)
		{
			link = null;
		}
	}
	
	/**
	 * @see com.yagasoft.overcast.container.Container#updateFromSource()
	 */
	@Override
	public void updateFromSource()
	{
		try
		{
			sourceObject = Google.driveService.files().get((sourceObject == null) ? id : sourceObject.getId()).execute();
			updateInfo();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * @see com.yagasoft.overcast.container.Container#copy(com.yagasoft.overcast.container.Folder,
	 *      boolean)
	 */
	@Override
	public Container<?> copy(Folder<?> destination, boolean overwrite)
	{
		return null;
	}
	
	/**
	 * @see com.yagasoft.overcast.container.Container#move(com.yagasoft.overcast.container.Folder,
	 *      boolean)
	 */
	@Override
	public void move(Folder<?> destination, boolean overwrite)
	{}
	
	/**
	 * @see com.yagasoft.overcast.container.Container#rename(java.lang.String)
	 */
	@Override
	public void rename(String newName)
	{}
	
	/**
	 * @see com.yagasoft.overcast.container.Container#delete()
	 */
	@Override
	public void delete()
	{
		try
		{
			Google.getDriveService().children().delete(parent.getId(), id).execute();
			parent.remove(this);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * @see com.yagasoft.overcast.container.IRemote#download(com.yagasoft.overcast.container.Folder,
	 *      boolean, com.yagasoft.overcast.container.ITransferProgressListener,
	 *      java.lang.Object)
	 */
	@Override
	public void download(Folder<?> parent, boolean overwrite, ITransferProgressListener listener, Object object) throws Exception
	{
		for (com.yagasoft.overcast.container.File<?> file : parent.getFilesArray())
		{
			if (file.getName().equals(name))
			{
				if (overwrite)
				{
					file.delete();
				}
				else
				{
					throw new Exception("File exists!");
				}
			}
		}
		
		try
		{
			addProgressListener(listener, object);
			
			OutputStream out = new FileOutputStream(new java.io.File(parent.getPath(), name));
			
			MediaHttpDownloader downloader = new MediaHttpDownloader(Google.getHttpTransport(), Google.driveService.getRequestFactory()
					.getInitializer());
			downloader.setDirectDownloadEnabled(false);
			downloader.setProgressListener(this);
			downloader.setChunkSize(MediaHttpUploader.MINIMUM_CHUNK_SIZE);
			
			downloader.download(new GenericUrl(link), out);
		}
		catch (IOException e)
		{
			e.printStackTrace();
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
				notifyListeners(TransferState.IN_PROGRESS, (float) downloader.getProgress());
				break;
			
			case MEDIA_COMPLETE:
				notifyListeners(TransferState.COMPLETED, (float) downloader.getProgress());
				break;
			
			default:
				System.out.println(downloader.getDownloadState());
				break;
		}
	}
	
}
