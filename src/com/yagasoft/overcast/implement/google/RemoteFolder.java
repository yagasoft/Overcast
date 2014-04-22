/*
 * Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
 *
 *		The Modified MIT Licence (GPL v3 compatible)
 * 			License terms are in a separate file (LICENCE.md)
 *
 *		Project/File: Overcast/com.yagasoft.overcast.implement.google/RemoteFolder.java
 *
 *			Modified: Apr 15, 2014 (2:06:59 PM)
 *			   Using: Eclipse J-EE / JDK 7 / Windows 8.1 x64
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
import com.yagasoft.overcast.base.container.operation.IOperationListener;
import com.yagasoft.overcast.base.container.operation.Operation;
import com.yagasoft.overcast.base.container.operation.OperationEvent;
import com.yagasoft.overcast.base.container.operation.OperationState;
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
	 * @see com.yagasoft.overcast.base.container.Folder#create(com.yagasoft.overcast.base.container.Folder, IOperationListener)
	 */
	@Override
	public synchronized void create(Folder<?> parent, IOperationListener listener) throws CreationException
	{
		Logger.info("creating folder: " + parent.getPath() + "/" + name);

		addOperationListener(listener, Operation.CREATE);

		RemoteFolder result = parent.searchByName(name, false);

		try
		{
			if (result != null)
			{
				Logger.error("creating folder -- already exists: " + parent.getPath() + "/" + name);
				throw new CreationException("Folder already Exists!");
			}

			File metadata = new File();
			metadata.setTitle(name);
			metadata.setMimeType("application/vnd.google-apps.folder");
			metadata.setParents(Arrays.asList(new ParentReference().setId(parent.getId())));

			Drive.Files.Insert insert = Google.driveService.files().insert(metadata);
			sourceObject = insert.execute();
			parent.add(this);
			notifyOperationListeners(Operation.CREATE, OperationState.COMPLETED, 1.0f);

			Logger.info("finished creating folder: " + path);
		}
		catch (IOException | CreationException e)
		{
			Logger.error("creating folder: " + parent.getPath() + "/" + name);
			Logger.except(e);
			e.printStackTrace();

			notifyOperationListeners(Operation.CREATE, OperationState.FAILED, 0f);
			throw new CreationException("Couldn't create folder! " + e.getMessage());
		}
		finally
		{
			clearOperationListeners(Operation.CREATE);
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
			return (Google.driveService.files().get((sourceObject == null) ? id : sourceObject.getId()).execute() != null);
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
	 * @see com.yagasoft.overcast.base.container.Folder#buildTree(int)
	 */
	@Override
	public synchronized void buildTree(final int numberOfLevels) throws OperationException
	{
		if (numberOfLevels < 0)
		{
			return;
		}

		Logger.info("building folder tree: " + path);

		try
		{
			List request = Google.getDriveService().files().list().setQ("trashed = false and '" + id + "' in parents");

			ArrayList<String> childrenIds = new ArrayList<String>();
			HashMap<String, File> children = new HashMap<String, File>();

			do
			{
				try
				{
					FileList childrenResult = request.execute();

					for (File child : childrenResult.getItems())
					{
						childrenIds.add(child.getId());
						children.put(child.getId(), child);
					}

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
						RemoteFolder folder = Google.factory.createFolder(remote, false);
						add(folder);

						Logger.info("found folder: " + folder.getPath());
					}
					else
					{
						RemoteFile file = Google.factory.createFile(remote, false);
						add(file);

						Logger.info("found file: " + file.getPath());
					}
				}
			}
		}
		catch (IOException | OperationException | CreationException e)
		{
			Logger.error("building folder tree: " + path);
			Logger.except(e);
			e.printStackTrace();

			throw new OperationException("Couldn't build tree! " + e.getMessage());
		}

		for (final Folder<?> folder : getFoldersArray())
		{
			try
			{
				new Thread(new Runnable()
				{

					@Override
					public void run()
					{
						try
						{
							slots.acquire();
							folder.buildTree(numberOfLevels - 1);		// build recursively.
							slots.release();
						}
						catch (InterruptedException | OperationException e)
						{
							e.printStackTrace();
						}
					}
				}).start();
			}
			catch (RuntimeException e)
			{
				Logger.error("building folder tree: " + folder.getPath());
				Logger.except(e);

				throw new OperationException(e.getMessage());
			}
		}

		Logger.info("finished building tree: " + path);
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
	 * @see com.yagasoft.overcast.base.container.Folder#updateInfo(boolean, boolean)
	 */
	@Override
	public synchronized void updateInfo(boolean folderContents, boolean recursively)
	{
		id = sourceObject.getId();
		name = sourceObject.getTitle();
		path = (((parent == null) || parent.getPath().equals("/")) ? "/" : (parent.getPath() + "/")) + name;
		// size = calculateSize(); // might be too heavy, so don't do it automatically.

		try
		{
			link = new URL(sourceObject.getSelfLink());
		}
		catch (MalformedURLException e)
		{
			link = null;
		}

		Logger.info("updated info: " + path);
	}

	/**
	 * @see com.yagasoft.overcast.base.container.Folder#updateFromSource(boolean, boolean)
	 */
	@Override
	public synchronized void updateFromSource(boolean folderContents, final boolean recursively) throws OperationException
	{
		Logger.info("updating info from source: " + path);

		// refresh children list.
		if (folderContents)
		{
			buildTree(recursively);
		}

		try
		{
			sourceObject = Google.driveService.files().get((sourceObject == null) ? id : sourceObject.getId()).execute();
			updateInfo();

			Logger.info("finished updating info from source: " + path);

			// refresh folder info without fetching the children list.
			if (recursively && !folderContents)
			{
				for (Folder<?> folder : getFoldersArray())
				{
					folder.updateFromSource(folderContents, recursively);
				}
			}
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
		Logger.info("copying folder: " + path);

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
					Logger.error("copying folder -- already exists: " + path);
					throw new OperationException("Folder already exists!");
				}
			}

			Google.driveService.parents().insert(id, new ParentReference().setId(((RemoteFolder) destination).getId())).execute();
			RemoteFile file = Google.getFactory().createFile(sourceObject, false);
			destination.add(file);
			notifyOperationListeners(Operation.COPY, OperationState.COMPLETED, 1.0f);

			Logger.info("finished copying to: " + destination.getPath());

			return file;
		}
		catch (IOException | OperationException | CreationException e)
		{
			Logger.error("copying folder: " + path);
			Logger.except(e);
			e.printStackTrace();

			notifyOperationListeners(Operation.COPY, OperationState.FAILED, 0.0f);
			throw new OperationException("Copy of folder failed! " + e.getMessage());
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
		Logger.info("moving folder: " + path);

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
					Logger.error("moving folder -- already exists: " + path);
					throw new OperationException("Folder already exists.");
				}
			}

			Google.driveService.parents().delete(id, parent.getId());
			Google.driveService.parents().insert(id, new ParentReference().setId(((RemoteFolder) destination).getId())).execute();
			parent.remove(this);
			destination.add(this);
			notifyOperationListeners(Operation.MOVE, OperationState.COMPLETED, 1.0f);

			Logger.info("finished moving to: " + destination.getPath());
		}
		catch (IOException | OperationException e)
		{
			Logger.error("moving folder: " + path);
			Logger.except(e);
			e.printStackTrace();

			notifyOperationListeners(Operation.MOVE, OperationState.FAILED, 0.0f);
			throw new OperationException("Move of folder failed! " + e.getMessage());
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
		Logger.info("renaming folder: " + path);

		addOperationListener(listener, Operation.RENAME);

		Container<?> existingFile = parent.searchByName(newName, false);

		try
		{
			if ((existingFile != null) && (existingFile instanceof RemoteFile))
			{
				Logger.error("renaming folder -- already exists: " + path);
				throw new OperationException("Folder already exists!");
			}

			sourceObject.setTitle(newName);
			Google.driveService.files().patch(id, sourceObject);
			notifyOperationListeners(Operation.RENAME, OperationState.COMPLETED, 1.0f);

			Logger.info("finished renaming folder: " + path);
		}
		catch (IOException | OperationException e)
		{
			Logger.error("renaming folder: " + path);
			Logger.except(e);
			e.printStackTrace();

			notifyOperationListeners(Operation.RENAME, OperationState.FAILED, 0.0f);
			throw new OperationException("Couldn't rename folder! " + e.getMessage());
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
		Logger.info("deleting folder: " + path);

		addOperationListener(listener, Operation.DELETE);

		try
		{
			Drive.Files.Delete delete = Google.driveService.files().delete(id);
			delete.execute();
			parent.remove(this);
			notifyOperationListeners(Operation.DELETE, OperationState.COMPLETED, 1.0f);

			Logger.info("finished deleting folder: " + path);
		}
		catch (IOException e)
		{
			Logger.error("deleting folder: " + path);
			Logger.except(e);
			e.printStackTrace();

			notifyOperationListeners(Operation.DELETE, OperationState.FAILED, 0f);
			throw new OperationException("Couldn't delete folder! " + e.getMessage());
		}
		finally
		{
			clearOperationListeners(Operation.DELETE);
		}
	}

	/**
	 * @see com.yagasoft.overcast.base.container.Container#updateInfo()
	 */
	@Override
	public synchronized void updateInfo()
	{
		updateInfo(false, false);
	}

	/**
	 * @see com.yagasoft.overcast.base.container.Container#updateFromSource()
	 */
	@Override
	public synchronized void updateFromSource() throws OperationException
	{
		updateFromSource(false, false);
	}

}
