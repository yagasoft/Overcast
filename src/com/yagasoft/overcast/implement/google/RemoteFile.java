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
import com.yagasoft.overcast.base.container.operation.IOperationListener;
import com.yagasoft.overcast.base.container.operation.Operation;
import com.yagasoft.overcast.base.container.operation.OperationEvent;
import com.yagasoft.overcast.base.container.operation.OperationState;
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

	}

	/**
	 * @see com.yagasoft.overcast.base.container.Container#isExist()
	 */
	@Override
	public synchronized boolean isExist() throws OperationException
	{
		Logger.newSection("checking existence of " + path);
		
		try
		{
			return (Google.driveService.files().get((sourceObject == null) ? id : sourceObject.getId()).execute() != null);
		}
		catch (IOException e)
		{
			Logger.newEntry("problem!");
			
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
		Logger.newSection("updating file info " + path);
		
		id = sourceObject.getId();
		name = sourceObject.getTitle();
		type = sourceObject.getMimeType();
		path = (((parent == null) || parent.getPath().equals("/")) ? "/" : (parent.getPath() + "/")) + name;

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
		
		Logger.newEntry("done!");
	}

	/**
	 * @see com.yagasoft.overcast.base.container.Container#updateFromSource()
	 */
	@Override
	public synchronized void updateFromSource() throws OperationException
	{
		Logger.newSection("updating file info from the csp " + path);
		
		try
		{
			sourceObject = Google.driveService.files().get((sourceObject == null) ? id : sourceObject.getId()).execute();
			updateInfo();
			
			Logger.newEntry("done!");
		}
		catch (IOException e)
		{
			Logger.newEntry("problem!");
			
			e.printStackTrace();
			throw new OperationException("Couldn't update info! " + e.getMessage());
		}
	}

	/**
	 * @see com.yagasoft.overcast.base.container.Container#copy(com.yagasoft.overcast.base.container.Folder, boolean, IOperationListener)
	 */
	@Override
	public synchronized Container<?> copy(Folder<?> destination, boolean overwrite, IOperationListener listener)
			throws OperationException
	{
		Logger.newSection("copying file " + path);
		
		addOperationListener(listener, Operation.COPY);

		Container<?> existingFile = destination.searchByName(name, false);

		try
		{
			if ((existingFile != null) && (existingFile instanceof RemoteFile))
			{
				if (overwrite)
				{
					existingFile.delete(new IOperationListener()
					{

						@Override
						public void operationProgressChanged(OperationEvent event)
						{}
					});
				}
				else
				{
					throw new OperationException("File already exists!");
				}
			}

			Google.driveService.parents().insert(id, new ParentReference().setId(((RemoteFolder) destination).getId())).execute();
			RemoteFile file = Google.getFactory().createFile(sourceObject, false);
			destination.add(file);
			notifyOperationListeners(Operation.COPY, OperationState.COMPLETED, 1.0f);
			
			Logger.newEntry("done!");
			
			return file;
		}
		catch (IOException | OperationException e)
		{
			Logger.newEntry("problem!");
			
			e.printStackTrace();
			notifyOperationListeners(Operation.COPY, OperationState.FAILED, 0.0f);
			throw new OperationException("Copy of file failed! " + e.getMessage());
		}
		finally
		{
			clearOperationListeners(Operation.COPY);
		}
	}

	/**
	 * @see com.yagasoft.overcast.base.container.Container#move(com.yagasoft.overcast.base.container.Folder, boolean, IOperationListener)
	 */
	@Override
	public synchronized void move(Folder<?> destination, boolean overwrite, IOperationListener listener)
			throws OperationException
	{
		Logger.newSection("moving file " + path);
		
		addOperationListener(listener, Operation.MOVE);

		Container<?> existingFile = destination.searchByName(name, false);

		try
		{
			if ((existingFile != null) && (existingFile instanceof RemoteFile))
			{
				if (overwrite)
				{
					existingFile.delete(new IOperationListener()
					{

						@Override
						public void operationProgressChanged(OperationEvent event)
						{}
					});
				}
				else
				{
					throw new OperationException("File already exists.");
				}
			}

			Google.driveService.parents().delete(id, parent.getId());
			Google.driveService.parents().insert(id, new ParentReference().setId(((RemoteFolder) destination).getId())).execute();
			parent.remove(this);
			destination.add(this);
			notifyOperationListeners(Operation.MOVE, OperationState.COMPLETED, 1.0f);
			
			Logger.newEntry("done!");
		}
		catch (IOException | OperationException e)
		{
			Logger.newEntry("problem!");
			
			e.printStackTrace();
			notifyOperationListeners(Operation.MOVE, OperationState.FAILED, 0.0f);
			throw new OperationException("Move of file failed! " + e.getMessage());
		}
		finally
		{
			clearOperationListeners(Operation.MOVE);
		}
	}

	/**
	 * @see com.yagasoft.overcast.base.container.Container#rename(java.lang.String, IOperationListener)
	 */
	@Override
	public synchronized void rename(String newName, IOperationListener listener) throws OperationException
	{
		Logger.newSection("renaming file " + path);
		
		addOperationListener(listener, Operation.RENAME);

		Container<?> existingFile = parent.searchByName(newName, false);

		try
		{
			if ((existingFile != null) && (existingFile instanceof RemoteFile))
			{
				throw new OperationException("File already exists!");
			}

			sourceObject.setTitle(newName);
			Google.driveService.files().patch(id, sourceObject);
			notifyOperationListeners(Operation.RENAME, OperationState.COMPLETED, 1.0f);
			
			Logger.newEntry("done!");
		}
		catch (IOException | OperationException e)
		{
			Logger.newEntry("problem!");
			
			e.printStackTrace();
			notifyOperationListeners(Operation.RENAME, OperationState.FAILED, 0.0f);
			throw new OperationException("Couldn't rename file! " + e.getMessage());
		}
		finally
		{
			clearOperationListeners(Operation.RENAME);
		}
	}

	/**
	 * @see com.yagasoft.overcast.base.container.Container#delete(IOperationListener)
	 */
	@Override
	public synchronized void delete(IOperationListener listener) throws OperationException
	{
		Logger.newSection("deleting file " + path);
		
		addOperationListener(listener, Operation.DELETE);

		try
		{
			Google.getDriveService().children().delete(parent.getId(), id).execute();
			parent.remove(this);
			notifyOperationListeners(Operation.DELETE, OperationState.COMPLETED, 1.0f);
			
			Logger.newEntry("done!");
		}
		catch (IOException e)
		{
			Logger.newEntry("problem!");
			
			e.printStackTrace();
			notifyOperationListeners(Operation.DELETE, OperationState.FAILED, 0.0f);
			throw new OperationException("Couldn't delete file! " + e.getMessage());
		}
		finally
		{
			clearOperationListeners(Operation.DELETE);
		}

	}

}
