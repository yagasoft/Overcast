/*
 * Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
 *
 *		The Modified MIT Licence (GPL v3 compatible)
 * 			License terms are in a separate file (LICENCE.md)
 *
 *		Project/File: Overcast/com.yagasoft.overcast.implement.google/RemoteFile.java
 *
 *			Modified: Apr 15, 2014 (2:02:31 PM)
 *			   Using: Eclipse J-EE / JDK 7 / Windows 8.1 x64
 */

package com.yagasoft.overcast.implement.google;


import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.ParentReference;
import com.yagasoft.logger.Logger;
import com.yagasoft.overcast.base.container.Container;
import com.yagasoft.overcast.base.container.Folder;
import com.yagasoft.overcast.exception.CreationException;
import com.yagasoft.overcast.exception.OperationException;


/**
 * @see com.yagasoft.overcast.base.container.remote.RemoteFile
 */
public class RemoteFile extends com.yagasoft.overcast.base.container.remote.RemoteFile<File>
{
	
	/**
	 * Better use the factory in Google class.
	 */
	public RemoteFile()
	{}
	
	/**
	 * @see com.yagasoft.overcast.base.container.Container#generateId()
	 */
	@Override
	public void generateId()
	{
		// TODO generate id
	}
	
	/**
	 * @see com.yagasoft.overcast.base.container.Container#isExist()
	 */
	@Override
	public synchronized boolean isExist() throws OperationException
	{
		Logger.info("checking existence: " + path);
		
		try
		{
			return (Google.driveService.files().get((getSourceObject() == null) ? id : getSourceObject().getId()).execute() != null);
		}
		catch (IOException e)
		{
			Logger.error("checking existence: " + path);
			Logger.except(e);
			e.printStackTrace();
			
			return false;
		}
	}
	
	/**
	 * @see com.yagasoft.overcast.base.container.Container#updateInfo()
	 */
	@Override
	public synchronized void updateInfo()
	{
		if (getSourceObject() != null)
		{
			id = getSourceObject().getId();
			name = getSourceObject().getTitle();
			type = getSourceObject().getMimeType();
			
			if (name == null)
			{
				name = "";
			}
			
			try
			{
				size = getSourceObject().getFileSize();
			}
			catch (Exception e)
			{
				size = 0;
			}
			
			try
			{
				link = new URL(getSourceObject().getDownloadUrl());
			}
			catch (MalformedURLException e)
			{
				link = null;
			}
		}
		
		path = (((parent == null) || parent.getPath().equals("/")) ? "/" : (parent.getPath() + "/")) + name;
		cleanPath();
		
		notifyUpdateListeners();
	}
	
	/**
	 * @see com.yagasoft.overcast.base.container.Container#updateFromSource()
	 */
	@Override
	public synchronized void updateFromSource() throws OperationException
	{
		Logger.info("updating info from source: " + path);
		
		try
		{
			setSourceObject(Google.driveService.files().get((getSourceObject() == null) ? id : getSourceObject().getId())
					.execute());
			
			Logger.info("finished updating info from source: " + path);
		}
		catch (IOException e)
		{
			Logger.error("updating info from source: " + path);
			Logger.except(e);
			e.printStackTrace();
			
			throw new OperationException("Couldn't update info! " + e.getMessage());
		}
	}
	
	/**
	 * @see com.yagasoft.overcast.base.container.Container#copyProcess(com.yagasoft.overcast.base.container.Folder)
	 */
	@Override
	protected Container<?> copyProcess(Folder<?> destination)
			throws OperationException
	{
		try
		{
			Google.driveService.parents().insert(id, new ParentReference().setId(destination.getId())).execute();
			return Google.getFactory().createFile(getSourceObject(), false);
		}
		catch (CreationException | IOException e)
		{
			throw new OperationException(e.getMessage());
		}
	}
	
	/**
	 * @see com.yagasoft.overcast.base.container.Container#moveProcess(com.yagasoft.overcast.base.container.Folder)
	 */
	@Override
	protected File moveProcess(Folder<?> destination)
			throws OperationException
	{
		try
		{
			Google.driveService.parents().insert(id, new ParentReference().setId(destination.getId())).execute();
			Google.driveService.parents().delete(id, getParent().getId()).execute();
			return getSourceObject();
		}
		catch (IOException e)
		{
			throw new OperationException(e.getMessage());
		}
	}
	
	/**
	 * @see com.yagasoft.overcast.base.container.Container#renameProcess(java.lang.String)
	 */
	@Override
	protected File renameProcess(String newName) throws OperationException
	{
		try
		{
			getSourceObject().setTitle(newName);
			Google.driveService.files().patch(id, getSourceObject()).execute();
			return getSourceObject();
		}
		catch (IOException e)
		{
			throw new OperationException(e.getMessage());
		}
	}
	
	/**
	 * @see com.yagasoft.overcast.base.container.Container#deleteProcess()
	 */
	@Override
	public void deleteProcess() throws OperationException
	{
		try
		{
			Google.getDriveService().children().delete(getParent().getId(), id).execute();
		}
		catch (IOException e)
		{
			throw new OperationException(e.getMessage());
		}
	}
	
}
