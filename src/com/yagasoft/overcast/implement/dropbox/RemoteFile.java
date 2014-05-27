/*
 * Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
 *
 *		The Modified MIT Licence (GPL v3 compatible)
 * 			Licence terms are in a separate file (LICENCE.md)
 *
 *		Project/File: Overcast/com.yagasoft.overcast.implement.dropbox/RemoteFile.java
 *
 *			Modified: 25-May-2014 (15:07:50)
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
import com.yagasoft.overcast.exception.AccessException;
import com.yagasoft.overcast.exception.CreationException;
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
	{
		// TODO generate id
	}

	/**
	 * @see com.yagasoft.overcast.base.container.Container#isExist()
	 */
	@Override
	public synchronized boolean isExist() throws AccessException
	{
		Logger.info("checking existence: " + path);

		// if fetching meta-data of the file fails, then it doesn't exist, probably.
		try
		{
			return (Dropbox.dropboxService.getMetadata((getSourceObject() == null) ? path : getSourceObject().path) != null);
		}
		catch (DbxException e)
		{
			Logger.error("checking existence: " + path);
			Logger.except(e);
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
		if (getSourceObject() != null)
		{
			id = getSourceObject().rev;
			name = getSourceObject().name;

			if (name == null)
			{
				name = "";
			}

			try
			{
				size = getSourceObject().numBytes;
			}
			catch (Exception e)
			{
				size = 0;
			}
		}

		type = null;

		path = (((parent == null) || parent.getPath().equals("/")) ? "/" : (parent.getPath() + "/")) + name;
		cleanPath();

		notifyOperationListeners();
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
			setSourceObject(Dropbox.dropboxService.getMetadata((getSourceObject() == null) ? path : getSourceObject().path)
					.asFile());

			try
			{
				link = new URL(Dropbox.dropboxService.createTemporaryDirectUrl(getSourceObject().path).url);
			}
			catch (MalformedURLException | DbxException | NullPointerException e)
			{
				link = null;
			}

			Logger.info("finished updating info from source: " + path);
		}
		catch (DbxException e)
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
			return Dropbox.getFactory().createFile(
					Dropbox.dropboxService.copy(path, destination.getPath() + "/" + name).asFile(), false);
		}
		catch (DbxException | CreationException e)
		{
			throw new OperationException(e.getMessage());
		}
			}

	/**
	 * @see com.yagasoft.overcast.base.container.Container#moveProcess(com.yagasoft.overcast.base.container.Folder)
	 */
	@Override
	protected DbxEntry.File moveProcess(Folder<?> destination)
			throws OperationException
	{
		try
		{
			return Dropbox.dropboxService.move(path, destination.getPath() + "/" + name).asFile();
		}
		catch (DbxException e)
		{
			throw new OperationException(e.getMessage());
		}
	}

	/**
	 * @see com.yagasoft.overcast.base.container.Container#renameProcess(java.lang.String)
	 */
	@Override
	protected DbxEntry.File renameProcess(String newName) throws OperationException
	{
		try
		{
			return Dropbox.dropboxService.move(path, getParent().getPath() + "/" + newName).asFile();
		}
		catch (DbxException e)
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
			Dropbox.dropboxService.delete(path);
		}
		catch (DbxException e)
		{
			throw new OperationException(e.getMessage());
		}
	}

}
