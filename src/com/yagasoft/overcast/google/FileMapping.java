
package com.yagasoft.overcast.google;


import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.NumberFormat;
import java.util.Arrays;

import com.google.api.client.googleapis.media.MediaHttpDownloader;
import com.google.api.client.googleapis.media.MediaHttpDownloaderProgressListener;
import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.googleapis.media.MediaHttpUploaderProgressListener;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.ParentReference;


public class FileMapping
		extends com.yagasoft.overcast.container.FileMapping<RemoteFile>
		implements MediaHttpUploaderProgressListener, MediaHttpDownloaderProgressListener
{

	private Drive	driveService;

	/**
	 *
	 */
	public FileMapping(Drive driveService)
	{
		setDriveService(driveService);
	}

	/**
	 * @see com.yagasoft.overcast.container.ContainerMapping#upload(String)
	 */
	@Override
	public void upload(String parentId)
	{
		File metadata = new File();
		metadata.setTitle(getLocal().getName());
		metadata.setMimeType(getLocal().getType());
		metadata.setParents(Arrays.asList(new ParentReference().setId(parentId)));

		FileContent content = new FileContent(getLocal().getType(), getLocal().getSourceObject().toFile());

		try
		{
			Drive.Files.Insert insert = driveService.files().insert(metadata, content);

			MediaHttpUploader uploader = insert.getMediaHttpUploader();
			uploader.setDirectUploadEnabled(false);
			uploader.setProgressListener(this);
			uploader.setChunkSize(MediaHttpUploader.MINIMUM_CHUNK_SIZE);

			setRemote(new RemoteFile(insert.execute()));
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * @see com.yagasoft.overcast.container.ContainerMapping#download(String)
	 */
	@Override
	public void download(String path)
	{
		try
		{
			OutputStream out = new FileOutputStream(new java.io.File(path, getRemote().getName()));

			MediaHttpDownloader downloader = new MediaHttpDownloader(Google.getHttpTransport(), driveService.getRequestFactory().getInitializer());
			downloader.setDirectDownloadEnabled(false);
			downloader.setProgressListener(this);
			downloader.setChunkSize(MediaHttpUploader.MINIMUM_CHUNK_SIZE);

			downloader.download(new GenericUrl(getRemote().getLink()), out);
		}
		catch (IOException e)
		{
			e.printStackTrace();
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
			case MEDIA_IN_PROGRESS:
				System.out.println("Progress: " + NumberFormat.getPercentInstance().format(uploader.getProgress()));
				break;

			default:
				System.out.println(uploader.getUploadState());
				break;
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
				System.out.println("Progress: " + NumberFormat.getPercentInstance().format(downloader.getProgress()));
				break;

			default:
				System.out.println(downloader.getDownloadState());
				break;
		}
	}

	/**
	 * @return the driveService
	 */
	public Drive getDriveService()
	{
		return driveService;
	}

	/**
	 * @param driveService
	 *            the driveService to set
	 */
	public void setDriveService(Drive driveService)
	{
		this.driveService = driveService;
	}

}

//class test extends Thread
//{
//	MediaHttpUploader uploader;
//
//	/**
//	 *
//	 */
//	public test(MediaHttpUploader uploader)
//	{
//		this.uploader = uploader;
//	}
//
//	/**
//	 * @see java.lang.Thread#run()
//	 */
//	@Override
//	public void run()
//	{
//		super.run();
//
//		double progress;
//
//		try
//		{
//			progress = uploader.getProgress();
//
//			while(progress < 1.0)
//			{
//				System.out.println(progress);
//				Thread.sleep(3000);
//				System.out.println(uploader.getMediaContent().getLength());
//				progress = uploader.getProgress();
//			}
//		}
//		catch (InterruptedException | IOException e)
//		{
//			e.printStackTrace();
//		}
//	}
//}
