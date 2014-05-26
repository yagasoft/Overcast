/*
 * Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
 * 
 *		The Modified MIT Licence (GPL v3 compatible)
 * 			License terms are in a separate file (LICENCE.md)
 * 
 *		Project/File: Overcast/com.yagasoft.overcast.implement.dropbox.transfer/Downloader.java
 * 
 *			Modified: Apr 15, 2014 (12:03:11 PM)
 *			   Using: Eclipse J-EE / JDK 7 / Windows 8.1 x64
 */

package com.yagasoft.overcast.implement.dropbox.transfer;


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
import com.yagasoft.overcast.base.container.transfer.TransferState;
import com.yagasoft.overcast.exception.TransferException;
import com.yagasoft.overcast.implement.dropbox.Dropbox;


/**
 * Dropbox doesn't handle downloading files automatically, so this class encapsulates the methods required.
 */
public class Downloader
{
	
	/** The size. */
	long							size;
	
	/** The remote file path. */
	String							remoteFilePath;
	
	/** The local parent path. */
	String							localParentPath;
	
	/** The local file. */
	Path							localFile;
	
	/** The download job. */
	DownloadJob						downloadJob;
	
	/** The output stream to the local file. */
	OutputStream					out;
	
	/** flag to stop the checking the downloaded amount, and to release resources. */
	boolean							done;
	
	/** The listeners to this download. */
	ArrayList<IProgressListener>	listeners	= new ArrayList<IProgressListener>();
	
	/**
	 * Instantiates a new downloader.
	 * 
	 * @param remoteFilePath
	 *            the remote file path
	 * @param localParentPath
	 *            the local parent path
	 * @param downloadJob
	 *            the download job
	 * @throws TransferException
	 *             the transfer exception
	 */
	public Downloader(String remoteFilePath, String localParentPath, DownloadJob downloadJob) throws TransferException
	{
		this.remoteFilePath = remoteFilePath;
		this.localParentPath = localParentPath;
		this.downloadJob = downloadJob;
		
		localFile = Paths.get(localParentPath + "/" + downloadJob.getSourceFile().getName());
		
		// fetch the file size to calculate download completion.
		size = downloadJob.getSourceFile().getSize();
	}
	
	/**
	 * Start the download based on the parameters set.
	 * 
	 * @return the source object file.
	 * @throws TransferException
	 *             the transfer exception
	 */
	public File startDownload() throws TransferException
	{
		Logger.info("started file download: " + remoteFilePath);
		
		try
		{
			// prepare the local file stream.
			out = Files.newOutputStream(localFile);
			
			// a separate thread to check on file progress by reading the size of the downloaded file locally.
			new Thread(() ->
			{
				long downloaded = 0;
				
				do
				{
					try
					{
						downloaded = Files.size(localFile);
						Logger.info("downloaded " + NumberFormat.getPercentInstance().format(downloaded / (double) size)
								+ " done. " + remoteFilePath);
						notifyProgressListeners(TransferState.IN_PROGRESS, (downloaded / (float) size));
						Thread.sleep(1000);
					}
					catch (IOException | InterruptedException e)
					{
						Logger.except(e);
						e.printStackTrace();
					}
				} while ( !done);
			}).start();
			
			notifyProgressListeners(TransferState.INITIALISED, 0.0f);
			
			return Dropbox.getDropboxService().getFile(remoteFilePath, null, out);
		}
		catch (IOException | DbxException e)
		{
			e.printStackTrace();
			
			if ( !done)
			{
				Logger.error("failed to download file: " + remoteFilePath);
				
				throw new TransferException("Failed to download file! " + e.getMessage());
			}
			else
			{
				Logger.info("cancelled download: " + remoteFilePath);
				
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
		for (IProgressListener listener : listeners)
		{
			listener.progressChanged(downloadJob, state, progress);
		}
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
