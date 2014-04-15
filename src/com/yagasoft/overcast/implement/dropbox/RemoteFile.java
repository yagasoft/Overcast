/* 
 * Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
 * 
 *		The Modified MIT Licence (GPL v3 compatible)
 * 			License terms are in a separate file (LICENCE.md)
 * 
 *		Project/File: Overcast/com.yagasoft.overcast.implement.dropbox/RemoteFile.java
 * 
 *			Modified: Apr 15, 2014 (9:50:40 AM)
 *			   Using: Eclipse J-EE / JDK 7 / Windows 8.1 x64
 */

package com.yagasoft.overcast.implement.dropbox;


import java.net.MalformedURLException;
import java.net.URL;

import com.dropbox.core.DbxEntry;
import com.dropbox.core.DbxException;
import com.yagasoft.logger.Logger;
import com.yagasoft.overcast.base.container.Container;
import com.yagasoft.overcast.base.container.Folder;
import com.yagasoft.overcast.base.container.operation.IOperationListener;
import com.yagasoft.overcast.base.container.operation.Operation;
import com.yagasoft.overcast.base.container.operation.OperationEvent;
import com.yagasoft.overcast.base.container.operation.OperationState;
import com.yagasoft.overcast.exception.AccessException;
import com.yagasoft.overcast.exception.OperationException;


/**
 * RemoteFile of Dropbox.
 * 
 * @see com.yagasoft.overcast.base.container.remote.RemoteFile
 */
public class RemoteFile extends com.yagasoft.overcast.base.container.remote.RemoteFile<DbxEntry.File>
{
	
	/**
	 * Better use the factory in Dropbox class.
	 */
	public RemoteFile()
	{}
	
	/**
	 * @see com.yagasoft.overcast.base.container.Container#generateId()
	 */
	@Override
	public void generateId()
	{}
	
	/**
	 * @see com.yagasoft.overcast.base.container.Container#isExist()
	 */
	@Override
	public synchronized boolean isExist() throws AccessException
	{
		Logger.newSection("checking existence of " + path);
		
		// if fetching meta-data of the file fails, then it doesn't exist, probably.
		try
		{
			return (Dropbox.dropboxService.getMetadata((sourceObject == null) ? path : sourceObject.path) != null);
		}
		catch (DbxException e)
		{
			Logger.newEntry("problem!");
			
			e.printStackTrace();
			throw new AccessException("Couldn't determine existence! " + e.getMessage());
		}
	}
	
	/**
	 * @see com.yagasoft.overcast.base.container.Container#updateInfo()
	 */
	@Override
	public synchronized void updateInfo()
	{
		Logger.newSection("updating file info " + path);
		
		id = sourceObject.rev;
		name = sourceObject.name;
		type = null;
		path = (((parent == null) || parent.getPath().equals("/")) ? "/" : (parent.getPath() + "/")) + name;
		
		try
		{
			size = sourceObject.numBytes;
		}
		catch (Exception e)
		{
			size = 0;
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
			sourceObject = Dropbox.dropboxService.getMetadata((sourceObject == null) ? path : sourceObject.path).asFile();
			updateInfo();
			
			try
			{
				link = new URL(Dropbox.dropboxService.createTemporaryDirectUrl(sourceObject.path).url);
			}
			catch (MalformedURLException | DbxException | NullPointerException e)
			{
				link = null;
			}
			
			Logger.newEntry("done!");
		}
		catch (DbxException e)
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
			
			Dropbox.dropboxService.copy(path, destination.getPath() + "/" + name);
			RemoteFile file = Dropbox.getFactory().createFile(sourceObject, false);
			destination.add(file);
			notifyOperationListeners(Operation.COPY, OperationState.COMPLETED, 1.0f);
			
			Logger.newEntry("done!");
			
			return file;
		}
		catch (DbxException | OperationException e)
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
			
			Dropbox.dropboxService.move(path, destination.getPath() + "/" + name);
			parent.remove(this);
			destination.add(this);
			notifyOperationListeners(Operation.MOVE, OperationState.COMPLETED, 1.0f);
			
			Logger.newEntry("done!");
		}
		catch (DbxException | OperationException e)
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
			
			Dropbox.dropboxService.move(path, parent.getPath() + "/" + newName);
			notifyOperationListeners(Operation.RENAME, OperationState.COMPLETED, 1.0f);
			
			Logger.newEntry("done!");
		}
		catch (DbxException | OperationException e)
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
			Dropbox.dropboxService.delete(path);
			parent.remove(this);
			notifyOperationListeners(Operation.DELETE, OperationState.COMPLETED, 1.0f);
			
			Logger.newEntry("done!");
		}
		catch (DbxException e)
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
