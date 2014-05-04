/*
 * Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
 *
 *		The Modified MIT Licence (GPL v3 compatible)
 * 			License terms are in a separate file (LICENCE.md)
 *
 *		Project/File: Overcast/com.yagasoft.overcast.implement.dropbox/RemoteFolder.java
 *
 *			Modified: Apr 15, 2014 (9:56:55 AM)
 *			   Using: Eclipse J-EE / JDK 7 / Windows 8.1 x64
 */

package com.yagasoft.overcast.implement.dropbox;


import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

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
import com.yagasoft.overcast.exception.CreationException;
import com.yagasoft.overcast.exception.OperationException;


/**
 * RemoteFolder of Dropbox.
 *
 * @see com.yagasoft.overcast.base.container.remote.RemoteFolder
 */
public class RemoteFolder extends com.yagasoft.overcast.base.container.remote.RemoteFolder<DbxEntry.Folder>
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

		// check if the folder exists in the parent ...
		Container<?> result = parent.searchByName(name, false)[0];

		try
		{
			// if it exists, problem!
			if ((result != null) && result.isFolder())
			{
				Logger.error("creating folder -- already exists: " + parent.getPath() + "/" + name);
				throw new CreationException("Folder already Exists!");
			}

			setSourceObject(Dropbox.dropboxService.createFolder(parent.getPath() + "/" + name));
			parent.add(this);
			notifyOperationListeners(Operation.CREATE, OperationState.COMPLETED, 1.0f);

			Logger.info("finished creating folder: " + path);
		}
		catch (DbxException | CreationException e)
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
			Logger.error("can't determine if folder exists or not: " + path);
			Logger.except(e);
			e.printStackTrace();

			throw new AccessException("Couldn't determine existence! " + e.getMessage());
		}
	}

	/**
	 * @see com.yagasoft.overcast.base.container.Folder#buildTree(int)
	 */
	@Override
	public synchronized void buildTree(final int numberOfLevels) throws OperationException
	{
		// no more levels to check.
		if (numberOfLevels < 0)
		{
			return;
		}

		try
		{
			// get folder list from Dropbox (metadata)
			DbxEntry.WithChildren listing = Dropbox.dropboxService.getMetadataWithChildren(path);

			//
			HashMap<String, DbxEntry> children = new HashMap<String, DbxEntry>();

			for (DbxEntry child : listing.children)
			{
				children.put(child.isFolder() ? child.path : ((DbxEntry.File) child).rev, child);
			}

			// collect the children IDs and filter already existing and deleted ones.
			ArrayList<String> childrenIds = new ArrayList<String>(children.keySet());
			removeObsolete(childrenIds, true);

			// if there're new children on the server ...
			if ( !childrenIds.isEmpty())
			{
				// check each one ...
				for (String child : childrenIds)
				{
					DbxEntry childAsEntry = children.get(child);

					// if the child is a folder ...
					if (childAsEntry.isFolder())
					{
						// create an object for it using the factory.
						RemoteFolder folder = Dropbox.factory.createFolder((DbxEntry.Folder) childAsEntry, false);
						add(folder);	// add it to this parent.

						Logger.info("found folder: " + folder.getPath());
					}
					else
					{
						RemoteFile file = Dropbox.factory.createFile((DbxEntry.File) childAsEntry, false);
						add(file);

						Logger.info("found file: " + file.getPath());
					}
				}
			}
		}
		catch (DbxException | CreationException e)
		{
			Logger.error("building folder tree: " + path);
			Logger.except(e);
			e.printStackTrace();

			throw new OperationException("Failed to build tree! " + e.getMessage());
		}

		// load sub-folders up to the level.
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
	 * @see com.yagasoft.overcast.base.container.Folder#updateInfo()
	 */
	@Override
	public synchronized void updateInfo()
	{
		super.updateInfo();

		id = getSourceObject().path;
		name = getSourceObject().name;
		// size = calculateSize(); // commented because it might be heavy, so better do it explicitly.

		Logger.info("updated info: " + path);
	}

	/**
	 * @see com.yagasoft.overcast.base.container.Folder#updateFromSource(boolean, boolean)
	 */
	@Override
	public synchronized void updateFromSource(boolean folderContents, boolean recursively) throws OperationException
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
			// re-fetch the meta-data from the server.
			setSourceObject(Dropbox.dropboxService.getMetadata((getSourceObject() == null) ? path : getSourceObject().path)
					.asFolder());

			try
			{
				// get link if available.
				link = new URL(Dropbox.dropboxService.createShareableUrl(getSourceObject().path));
			}
			catch (MalformedURLException | DbxException e)
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
	 * @see com.yagasoft.overcast.base.container.Container#updateFromSource()
	 */
	@Override
	public synchronized void updateFromSource() throws OperationException
	{
		updateFromSource(true, false);
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

		Container<?> existingFolder = destination.searchByName(name, false)[0];

		try
		{
			if ((existingFolder != null) && existingFolder.isFolder())
			{
				if (overwrite)
				{
					existingFolder.delete(new IOperationListener()
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

			Dropbox.dropboxService.copy(path, destination.getPath() + "/" + name);
			RemoteFolder file = Dropbox.getFactory().createFolder(getSourceObject(), false);
			destination.add(file);
			notifyOperationListeners(Operation.COPY, OperationState.COMPLETED, 1.0f);

			Logger.info("finished copying to: " + destination.getPath());

			return file;
		}
		catch (DbxException | OperationException | CreationException e)
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

		Container<?> existingFolder = destination.searchByName(name, false)[0];

		try
		{
			if ((existingFolder != null) && existingFolder.isFolder())
			{
				if (overwrite)
				{
					existingFolder.delete(new IOperationListener()
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

			Dropbox.dropboxService.move(path, destination.getPath() + "/" + name);
			getParent().remove(this);
			destination.add(this);
			notifyOperationListeners(Operation.MOVE, OperationState.COMPLETED, 1.0f);

			Logger.info("finished moving to: " + destination.getPath());
		}
		catch (DbxException | OperationException e)
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

		Container<?> existingFolder = getParent().searchByName(newName, false)[0];

		try
		{
			if ((existingFolder != null) && existingFolder.isFolder())
			{
				Logger.error("renaming folder -- already exists: " + path);
				throw new OperationException("Folder already exists!");
			}

			Dropbox.dropboxService.move(path, getParent().getPath() + "/" + newName);
			notifyOperationListeners(Operation.RENAME, OperationState.COMPLETED, 1.0f);
			notifyUpdateListeners();

			Logger.info("finished renaming folder: " + path);
		}
		catch (DbxException | OperationException e)
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
			Dropbox.dropboxService.delete(path);
			getParent().remove(this);
			notifyOperationListeners(Operation.DELETE, OperationState.COMPLETED, 1.0f);

			Logger.info("finished deleting folder: " + path);
		}
		catch (DbxException e)
		{
			Logger.error("deleting folder: " + path);
			Logger.except(e);
			e.printStackTrace();

			notifyOperationListeners(Operation.DELETE, OperationState.FAILED, 0.0f);
			throw new OperationException("Couldn't delete folder! " + e.getMessage());
		}
		finally
		{
			clearOperationListeners(Operation.DELETE);
		}

	}

}
