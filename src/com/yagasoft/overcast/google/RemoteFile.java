/* 
 * Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
 * 
 *		Modified MIT License (GPL v3 compatible)
 * 			License terms are in a separate file (license.txt)
 * 
 *		Project/File: Overcast/com.yagasoft.overcast.google/RemoteFile.java
 * 
 *			Modified: 27-Mar-2014 (16:15:03)
 *			   Using: Eclipse J-EE / JDK 7 / Windows 8.1 x64
 */

package com.yagasoft.overcast.google;


import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import com.google.api.services.drive.model.File;
import com.yagasoft.overcast.container.Container;
import com.yagasoft.overcast.container.Folder;
import com.yagasoft.overcast.container.operation.IOperationListener;
import com.yagasoft.overcast.container.operation.Operation;
import com.yagasoft.overcast.container.operation.OperationState;
import com.yagasoft.overcast.exception.OperationException;


public class RemoteFile extends com.yagasoft.overcast.container.remote.RemoteFile<File>
{
	
	/**
	 * Better use the factory in Google class.
	 */
	public RemoteFile()
	{}
	
	/**
	 * @see com.yagasoft.overcast.container.Container#generateId()
	 */
	@Override
	public void generateId()
	{	
		
	}
	
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
		path = ((parent == null) ? "/" : (parent.getPath())) + name;
		
		try
		{
			size = sourceObject.getFileSize();
		}
		catch (Exception e)
		{
			size = 0;
		}
		
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
	 * @see com.yagasoft.overcast.container.Container#copy(com.yagasoft.overcast.container.Folder, boolean, IOperationListener)
	 */
	@Override
	public Container<?> copy(Folder<?> destination, boolean overwrite, IOperationListener listener) throws OperationException
	{
		return null;
	}
	
	/**
	 * @see com.yagasoft.overcast.container.Container#move(com.yagasoft.overcast.container.Folder, boolean, IOperationListener)
	 */
	@Override
	public void move(Folder<?> destination, boolean overwrite, IOperationListener listener) throws OperationException
	{}
	
	/**
	 * @see com.yagasoft.overcast.container.Container#rename(java.lang.String, IOperationListener)
	 */
	@Override
	public void rename(String newName, IOperationListener listener) throws OperationException
	{}
	
	/**
	 * @see com.yagasoft.overcast.container.Container#delete(IOperationListener)
	 */
	@Override
	public void delete(IOperationListener listener) throws OperationException
	{
		addOperationListener(listener, Operation.DELETE);
		
		try
		{
			new Thread(new Runnable()
			{
				
				@Override
				public void run()
				{
					try
					{
						Google.getDriveService().children().delete(parent.getId(), id).execute();
						parent.remove(RemoteFile.this);
						notifyOperationListeners(Operation.DELETE, OperationState.COMPLETED, 1.0f);
					}
					catch (IOException e)
					{
						e.printStackTrace();
						notifyOperationListeners(Operation.DELETE, OperationState.FAILED, 0.0f);
						throw new RuntimeException("Couldn't delete file.");
					}
				}
				
			}).start();
		}
		catch (RuntimeException e)
		{
			throw new OperationException(e.getMessage());
		}
	}
	
}
