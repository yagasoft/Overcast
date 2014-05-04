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
		super.updateInfo();

		if (getSourceObject() != null)
		{
			id = getSourceObject().getId();
			name = getSourceObject().getTitle();
			type = getSourceObject().getMimeType();

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

		Logger.info("updated info: " + path);
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
	 * @see com.yagasoft.overcast.base.container.Container#copy(com.yagasoft.overcast.base.container.Folder, boolean,
	 *      IOperationListener)
	 */
	@Override
	public synchronized Container<?> copy(Folder<?> destination, boolean overwrite, IOperationListener listener)
			throws OperationException
	{
		Logger.info("copying file: " + path);

		addOperationListener(listener, Operation.COPY);

		Container<?>[] existingFile = destination.searchByName(name, false);

		try
		{
			if ((existingFile.length > 0) && !existingFile[0].isFolder())
			{
				if (overwrite)
				{
					existingFile[0].delete(new IOperationListener()
					{

						@Override
						public void operationProgressChanged(OperationEvent event)
						{}
					});
				}
				else
				{
					Logger.error("copying file -- already exists: " + path);
					throw new OperationException("File already exists!");
				}
			}

			Google.driveService.parents().insert(id, new ParentReference().setId(((RemoteFolder) destination).getId())).execute();
			RemoteFile file = Google.getFactory().createFile(getSourceObject(), false);
			destination.add(file);
			notifyOperationListeners(Operation.COPY, OperationState.COMPLETED, 1.0f);

			Logger.info("finished copying to: " + destination.getPath());

			return file;
		}
		catch (IOException | OperationException | CreationException e)
		{
			Logger.error("copying file: " + path);
			Logger.except(e);
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
	 * @see com.yagasoft.overcast.base.container.Container#move(com.yagasoft.overcast.base.container.Folder, boolean,
	 *      IOperationListener)
	 */
	@Override
	public synchronized void move(Folder<?> destination, boolean overwrite, IOperationListener listener)
			throws OperationException
	{
		Logger.info("moving file: " + path);

		addOperationListener(listener, Operation.MOVE);

		Container<?>[] existingFile = destination.searchByName(name, false);

		try
		{
			if ((existingFile.length > 0) && !existingFile[0].isFolder())
			{
				if (overwrite)
				{
					existingFile[0].delete(new IOperationListener()
					{

						@Override
						public void operationProgressChanged(OperationEvent event)
						{}
					});
				}
				else
				{
					Logger.error("moving file -- already exists: " + path);
					throw new OperationException("File already exists.");
				}
			}

			Google.driveService.parents().delete(id, getParent().getId());
			Google.driveService.parents().insert(id, new ParentReference().setId(((RemoteFolder) destination).getId())).execute();
			getParent().remove(this);
			destination.add(this);
			notifyOperationListeners(Operation.MOVE, OperationState.COMPLETED, 1.0f);

			Logger.info("finished moving to: " + destination.getPath());
		}
		catch (IOException | OperationException e)
		{
			Logger.error("moving file: " + path);
			Logger.except(e);
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
		Logger.info("renaming file: " + path);

		addOperationListener(listener, Operation.RENAME);

		Container<?>[] existingFile = getParent().searchByName(newName, false);

		try
		{
			if ((existingFile.length > 0) && !existingFile[0].isFolder())
			{
				Logger.error("renaming file -- already exists: " + path);
				throw new OperationException("File already exists!");
			}

			getSourceObject().setTitle(newName);
			Google.driveService.files().patch(id, getSourceObject());
			notifyOperationListeners(Operation.RENAME, OperationState.COMPLETED, 1.0f);
			notifyUpdateListeners();

			Logger.info("finished renaming file: " + path);
		}
		catch (IOException | OperationException e)
		{
			Logger.error("renaming file: " + path);
			Logger.except(e);
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
		Logger.info("deleting file: " + path);

		addOperationListener(listener, Operation.DELETE);

		try
		{
			Google.getDriveService().children().delete(getParent().getId(), id).execute();
			getParent().remove(this);
			notifyOperationListeners(Operation.DELETE, OperationState.COMPLETED, 1.0f);

			Logger.info("finished deleting file: " + path);
		}
		catch (IOException e)
		{
			Logger.error("deleting file: " + path);
			Logger.except(e);
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
