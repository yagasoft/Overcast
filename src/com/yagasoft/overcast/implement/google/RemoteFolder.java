/*
 * Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
 *
 *		The Modified MIT Licence (GPL v3 compatible)
 * 			Licence terms are in a separate file (LICENCE.md)
 *
 *		Project/File: Overcast/com.yagasoft.overcast.implement.google/RemoteFolder.java
 *
 *			Modified: 26-May-2014 (22:05:07)
 *			   Using: Eclipse J-EE / JDK 8 / Windows 8.1 x64
 */

package com.yagasoft.overcast.implement.google;


import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.Drive.Files.List;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.ParentReference;
import com.yagasoft.logger.Logger;
import com.yagasoft.overcast.base.container.Container;
import com.yagasoft.overcast.base.container.Folder;
import com.yagasoft.overcast.exception.CreationException;
import com.yagasoft.overcast.exception.OperationException;


/**
 * @see com.yagasoft.overcast.base.container.remote.RemoteFolder
 */
public class RemoteFolder extends com.yagasoft.overcast.base.container.remote.RemoteFolder<File>
{

	/**
	 * Better use the factory in Google class.
	 */
	public RemoteFolder()
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
	 * @see com.yagasoft.overcast.base.container.Folder#createProcess(com.yagasoft.overcast.base.container.Folder)
	 */
	@Override
	public File createProcess(Folder<?> parent) throws CreationException
	{
		try
		{
			File metadata = new File();
			metadata.setTitle(name);
			metadata.setMimeType("application/vnd.google-apps.folder");
			metadata.setParents(Arrays.asList(new ParentReference().setId(parent.getId())));

			Drive.Files.Insert insert = Google.driveService.files().insert(metadata);

			return insert.execute();
		}
		catch (IOException e)
		{
			throw new CreationException(e.getMessage());
		}
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
			Logger.error("can't determine if folder exists or not: " + path);
			Logger.except(e);
			e.printStackTrace();

			return false;
		}
	}

	/**
	 * @see com.yagasoft.overcast.base.container.Folder#buildTreeProcess(int, java.util.List)
	 */
	@Override
	protected void buildTreeProcess(final int numberOfLevels, java.util.List<Container<?>> childrenArray)
			throws OperationException
	{
		try
		{
			List request = Google.getDriveService().files().list().setQ("trashed = false and '" + id + "' in parents");

			ArrayList<String> childrenIds = new ArrayList<String>();
			// id and child
			HashMap<String, File> children = new HashMap<String, File>();

			do
			{
				try
				{
					FileList childrenResult = request.execute();

					childrenResult.getItems().parallelStream()
						.forEach(child ->
						{
							childrenIds.add(child.getId());
							children.put(child.getId(), child);
						});

					request.setPageToken(childrenResult.getNextPageToken());
				}
				catch (IOException e)
				{
					request.setPageToken(null);
					e.printStackTrace();
					throw new OperationException(e.getMessage());
				}
			} while ((request.getPageToken() != null) && (request.getPageToken().length() > 0));

			removeObsolete(childrenIds, true);

			if ( !childrenIds.isEmpty())
			{
				for (String id : childrenIds)
				{
					File remote = children.get(id);

					if (remote.getMimeType().indexOf("folder") >= 0)
					{
						childrenArray.add(Google.factory.createFolder(remote, false));
					}
					else
					{
						childrenArray.add(Google.factory.createFile(remote, false));
					}
				}
			}
		}
		catch (IOException | CreationException e)
		{
			throw new OperationException(e.getMessage());
		}
	}

	/**
	 * @see com.yagasoft.overcast.base.container.Folder#calculateSize()
	 */
	@Override
	public synchronized long calculateSize() throws OperationException
	{
		return 0;
	}

	/**
	 * @see com.yagasoft.overcast.base.container.Folder#updateInfo()
	 */
	@Override
	public synchronized void updateInfo()
	{
		if (getSourceObject() != null)
		{
			id = getSourceObject().getId();
			name = getSourceObject().getTitle();

			if (name == null)
			{
				name = "";
			}

			try
			{
				link = new URL(getSourceObject().getSelfLink());
			}
			catch (MalformedURLException e)
			{
				link = null;
			}
		}

		// size = calculateSize(); // might be too heavy, so don't do it automatically.

		path = (((parent == null) || parent.getPath().equals("/")) ? "/" : (parent.getPath() + "/")) + name;
		cleanPath();

		notifyOperationListeners();
	}

	/**
	 * @see com.yagasoft.overcast.base.container.Folder#updateFromSource(boolean, boolean)
	 */
	@Override
	public synchronized void updateFromSource(boolean folderContents, final boolean recursively) throws OperationException
	{
		// go through all the children. This is done first thing so that it doesn't load the tree recursively!
		if (recursively)
		{
			for (Folder<?> folder : getFoldersArray())
			{
				folder.updateFromSource(folderContents, recursively);
			}
		}

		Logger.info("updating info from source: " + path);

		// refresh children list.
		if (folderContents)
		{
			buildTree(false);
		}

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
	 * @see com.yagasoft.overcast.base.container.Container#updateFromSource()
	 */
	@Override
	public synchronized void updateFromSource() throws OperationException
	{
		updateFromSource(true, false);
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
			return Google.factory.createFolder(getSourceObject(), false);
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
			// Google.driveService.files().delete(id).execute();
		}
		catch (IOException e)
		{
			throw new OperationException(e.getMessage());
		}
	}

}
