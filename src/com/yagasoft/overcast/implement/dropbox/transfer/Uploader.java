/*
 * Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
 *
 *		Modified MIT License (GPL v3 compatible)
 * 			License terms are in a separate file (license.txt)
 *
 *		Project/File: Overcast/com.yagasoft.overcast.dropbox/Uploader.java
 *
 *			Modified: 11-Apr-2014 (23:20:03)
 *			   Using: Eclipse J-EE / JDK 7 / Windows 8.1 x64
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
import com.yagasoft.overcast.base.container.transfer.TransferState;
import com.yagasoft.overcast.exception.TransferException;
import com.yagasoft.overcast.implement.dropbox.Dropbox;


public class Uploader
{

	long							size;
	int								chunkSize	= 16 * 1024;
	String							remoteParentPath;
	String							localFilePath;
	Path							localFile;
	UploadJob						uploadJob;
	boolean							stop		= false;

	ArrayList<IProgressListener>	listeners	= new ArrayList<IProgressListener>();

	/**
	 * Instantiates a new uploader.
	 *
	 * @param remoteParent
	 *            Remote parent.
	 * @param localFilePath
	 *            Local file path.
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
			size = Files.size(localFile);
		}
		catch (IOException e)
		{
			e.printStackTrace();
			throw new TransferException("Failed to upload file! " + e.getMessage());
		}
	}

	public File startUpload() throws TransferException
	{
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
			Logger.newSection("Uploaded first chunk!");
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
				Logger.post("Uploaded " + (offset / chunkSize) + " / " + (size / chunkSize)
						+ " => " + NumberFormat.getPercentInstance().format(offset / (double) size) + " done.");
				notifyProgressListeners(TransferState.IN_PROGRESS, (offset / (float) size));
				buffer.clear();
			}

			if (stop)
			{
				notifyProgressListeners(TransferState.CANCELLED, 0.0f);
				return null;
			}

			// the file uploaded is in the void; move it to the proper path.
			return Dropbox.getDropboxService().chunkedUploadFinish(remoteParentPath + "/" + localFile.getFileName().toString()
					, DbxWriteMode.add(), uploadId);
		}
		catch (IOException | DbxException e)
		{
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

	public void addProgressListener(IProgressListener listener)
	{
		listeners.add(listener);
	}

	public void notifyProgressListeners(TransferState state, float progress)
	{
		for (IProgressListener listener : listeners)
		{
			listener.progressChanged(uploadJob, state, progress);
		}
	}

	public void removeProgressListener(IProgressListener listener)
	{
		listeners.remove(listener);
	}

	public void clearProgressListeners()
	{
		listeners.clear();
	}

	public void cancel()
	{
		stop = true;
	}
}
