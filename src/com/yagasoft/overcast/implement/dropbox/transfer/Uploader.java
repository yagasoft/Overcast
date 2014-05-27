/* 
 * Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
 * 
 *		The Modified MIT Licence (GPL v3 compatible)
 * 			Licence terms are in a separate file (LICENCE.md)
 * 
 *		Project/File: Overcast/com.yagasoft.overcast.implement.dropbox.transfer/Uploader.java
 * 
 *			Modified: 26-May-2014 (22:03:44)
 *			   Using: Eclipse J-EE / JDK 8 / Windows 8.1 x64
 */

package com.yagasoft.overcast.implement.dropbox.transfer;


import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.util.ArrayList;

import com.dropbox.core.DbxEntry.File;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxWriteMode;
import com.yagasoft.logger.Logger;
import com.yagasoft.overcast.base.container.transfer.event.TransferState;
import com.yagasoft.overcast.exception.TransferException;
import com.yagasoft.overcast.implement.dropbox.Dropbox;


/**
 * Dropbox doesn't handle uploading files automatically, so this class encapsulates the methods required.
 */
public class Uploader
{

	/** The size. */
	long							size;
	
	/** The chunk size. */
	int								chunkSize	= 16 * 1024;
	
	/** The remote parent path. */
	String							remoteParentPath;
	
	/** The local file path. */
	String							localFilePath;
	
	/** The local file. */
	Path							localFile;
	
	/** The upload job. */
	UploadJob						uploadJob;
	
	/** flag to stop uploading. */
	boolean							stop		= false;

	/** The listeners to this upload. */
	ArrayList<IProgressListener>	listeners	= new ArrayList<IProgressListener>();

	/**
	 * Instantiates a new uploader.
	 * 
	 * @param remoteParentPath
	 *            the remote parent path
	 * @param localFilePath
	 *            Local file path.
	 * @param uploadJob
	 *            the upload job
	 * @throws TransferException
	 *             the transfer exception
	 */
	public Uploader(String remoteParentPath, String localFilePath, UploadJob uploadJob) throws TransferException
	{
		this.remoteParentPath = remoteParentPath;
		this.localFilePath = localFilePath;
		this.uploadJob = uploadJob;

		localFile = Paths.get(localFilePath);

		try
		{
			// fetch the file size to calculate upload completion.
			size = Files.size(localFile);
		}
		catch (IOException e)
		{
			e.printStackTrace();
			throw new TransferException("Failed to upload file! " + e.getMessage());
		}
	}

	/**
	 * Start the upload based on the parameters set.
	 * 
	 * @return the file
	 * @throws TransferException
	 *             the transfer exception
	 */
	public File startUpload() throws TransferException
	{
		Logger.info("started file upload: " + localFilePath);
		
		// use buffers to read the file in case the file is too big for simpler API methods.
		RandomAccessFile file = null;
		FileChannel channel = null;

		try
		{
			long offset = 0;

			// prepare file channel and buffer to read the file chunks.
			file = new RandomAccessFile(localFilePath, "r");
			channel = file.getChannel();
			ByteBuffer buffer = ByteBuffer.allocate(chunkSize);

			// read first chunk and upload it.
			channel.read(buffer);
			buffer.flip();
			notifyProgressListeners(TransferState.INITIALISED, 0.0f);
			String uploadId = Dropbox.getDropboxService().chunkedUploadFirst(buffer.array());
			
			Logger.info("uploaded first chunk!");
			
			offset += buffer.remaining();		// increment offset; used for remote marker.
			notifyProgressListeners(TransferState.IN_PROGRESS, (offset / (float) size));
			buffer.clear();

			// upload the rest of the file chunks.
			while (channel.read(buffer) > 0)
			{
				if (stop)
				{
					break;
				}

				buffer.flip();
				Dropbox.getDropboxService().chunkedUploadAppend(uploadId, offset, buffer.array());
				offset += buffer.remaining();
				Logger.info("uploaded " + (offset / chunkSize) + " / " + (size / chunkSize)
						+ " => " + NumberFormat.getPercentInstance().format(offset / (double) size) + " done. " + localFilePath);
				notifyProgressListeners(TransferState.IN_PROGRESS, (offset / (float) size));
				buffer.clear();
			}

			if (stop)
			{
				Logger.info("upload cancelled: " + localFilePath);
				
				notifyProgressListeners(TransferState.CANCELLED, 0.0f);
				return null;
			}

			// the file uploaded is in the void; move it to the proper path.
			return Dropbox.getDropboxService().chunkedUploadFinish(remoteParentPath + "/" + localFile.getFileName().toString()
					, DbxWriteMode.add(), uploadId);
		}
		catch (IOException | DbxException e)
		{
			Logger.error("upload: " + localFilePath);
			Logger.except(e);
			e.printStackTrace();
			
			throw new TransferException("Failed to upload file! " + e.getMessage());
		}
		finally
		{
			// release resources.
			try
			{
				channel.close();
				file.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * Adds the progress listener.
	 * 
	 * @param listener
	 *            the listener
	 */
	public void addProgressListener(IProgressListener listener)
	{
		listeners.add(listener);
	}

	/**
	 * Notify progress listeners.
	 * 
	 * @param state
	 *            the state
	 * @param progress
	 *            the progress
	 */
	public void notifyProgressListeners(TransferState state, float progress)
	{
		listeners.parallelStream()
			.forEach(listener -> listener.progressChanged(uploadJob, state, progress));
	}

	/**
	 * Removes the progress listener.
	 * 
	 * @param listener
	 *            the listener
	 */
	public void removeProgressListener(IProgressListener listener)
	{
		listeners.remove(listener);
	}

	/**
	 * Clear progress listeners.
	 */
	public void clearProgressListeners()
	{
		listeners.clear();
	}

	/**
	 * Cancel.
	 */
	public void cancel()
	{
		stop = true;
	}
}
