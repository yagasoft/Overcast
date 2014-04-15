/* 
 * Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
 * 
 *		The Modified MIT Licence (GPL v3 compatible)
 * 			License terms are in a separate file (LICENCE.md)
 * 
 *		Project/File: Overcast/com.yagasoft.overcast.implement.dropbox.transfer/IProgressListener.java
 * 
 *			Modified: Apr 15, 2014 (1:20:28 PM)
 *			   Using: Eclipse J-EE / JDK 7 / Windows 8.1 x64
 */

package com.yagasoft.overcast.implement.dropbox.transfer;

import com.yagasoft.overcast.base.container.transfer.TransferState;




/**
 * The listener interface for receiving IProgress events.
 * The class that is interested in processing a IProgress
 * event implements this interface, and the object created
 * with that class is registered with a component using the
 * component's <code>addIProgressListener<code> method. When
 * the IProgress event occurs, that object's appropriate
 * method is invoked.
 * 
 * @see IProgressEvent
 */
public interface IProgressListener
{
	
	/**
	 * Progress changed.
	 * 
	 * @param uploadJob
	 *            the upload job
	 * @param state
	 *            the state
	 * @param progress
	 *            the progress
	 */
	public void progressChanged(UploadJob uploadJob, TransferState state, float progress);

	/**
	 * Progress changed.
	 * 
	 * @param downloadJob
	 *            the download job
	 * @param state
	 *            the state
	 * @param progress
	 *            the progress
	 */
	public void progressChanged(DownloadJob downloadJob, TransferState state, float progress);
}
