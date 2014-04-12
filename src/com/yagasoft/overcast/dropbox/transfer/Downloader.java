/*
 * Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
 *
 *		Modified MIT License (GPL v3 compatible)
 * 			License terms are in a separate file (license.txt)
 *
 *		Project/File: Overcast/com.yagasoft.overcast.dropbox/Downloader.java
 *
 *			Modified: 12-Apr-2014 (03:13:23)
 *			   Using: Eclipse J-EE / JDK 7 / Windows 8.1 x64
 */

package com.yagasoft.overcast.dropbox.transfer;


import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.util.ArrayList;

import com.dropbox.core.DbxEntry.File;
import com.dropbox.core.DbxException;
import com.yagasoft.logger.Logger;
import com.yagasoft.overcast.container.transfer.TransferState;
import com.yagasoft.overcast.dropbox.Dropbox;
import com.yagasoft.overcast.exception.TransferException;


public class Downloader
{

	long							size;
	String							remoteFilePath;
	String							localParentPath;
	Path							localFile;
	DownloadJob						downloadJob;
	OutputStream					out;
	boolean done;

	ArrayList<IProgressListener>	listeners	= new ArrayList<IProgressListener>();

	/**
	 * Instantiates a new downloader.
	 *
	 * @param remoteParent
	 *            Remote parent.
	 * @param localFilePath
	 *            Local file path.
	 * @throws TransferException
	 *             the transfer exception
	 */
	public Downloader(String remoteFilePath, String localParentPath, DownloadJob downloadJob) throws TransferException
	{
		this.remoteFilePath = remoteFilePath;
		this.localParentPath = localParentPath;
		this.downloadJob = downloadJob;
		localFile = Paths.get(localParentPath + "\\" + downloadJob.getSourceFile().getName());
		size = downloadJob.getSourceFile().getSize();
	}

	public File startDownload() throws TransferException
	{
		try
		{
			out = Files.newOutputStream(localFile);

			// a separate thread to check on file progress by reading the size of the downloaded file locally.
			new Thread(new Runnable()
			{

				@Override
				public void run()
				{
					long downloaded = 0;

					do
					{
						try
						{
							downloaded = Files.size(localFile);
							Logger.post("Downloaded " + NumberFormat.getPercentInstance().format(downloaded / (double) size)
									+ " done.");
							notifyProgressListeners(TransferState.IN_PROGRESS, (downloaded / (float) size));
							Thread.sleep(1000);
						}
						catch (IOException | InterruptedException e)
						{
							e.printStackTrace();
						}
					} while (!done);
				}
			}).start();

			notifyProgressListeners(TransferState.INITIALISED, 0.0f);

			return Dropbox.getDropboxService().getFile(remoteFilePath, null, out);
		}
		catch (IOException | DbxException e)
		{
			e.printStackTrace();

			if (!done)
			{
				throw new TransferException("Failed to download file! " + e.getMessage());
			}
			else
			{
				notifyProgressListeners(TransferState.CANCELLED, 0.0f);
				return null;
			}
		}
		finally
		{
			// release resources.
			try
			{
				done = true;
				out.close();
			}
			catch (IOException e)
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
			listener.progressChanged(downloadJob, state, progress);
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
		try
		{
			done = true;
			out.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
