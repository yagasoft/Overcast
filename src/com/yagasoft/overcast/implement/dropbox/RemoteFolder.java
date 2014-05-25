/*
 * Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
 *
 *		The Modified MIT Licence (GPL v3 compatible)
 * 			Licence terms are in a separate file (LICENCE.md)
 *
 *		Project/File: Overcast/com.yagasoft.overcast.implement.dropbox/RemoteFolder.java
 *
 *			Modified: 25-May-2014 (21:19:19)
 *			   Using: Eclipse J-EE / JDK 7 / Windows 8.1 x64
 */

package com.yagasoft.overcast.implement.dropbox;


import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.dropbox.core.DbxEntry;
import com.dropbox.core.DbxException;
import com.yagasoft.logger.Logger;
import com.yagasoft.overcast.base.container.Container;
import com.yagasoft.overcast.base.container.Folder;
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
	 * @see com.yagasoft.overcast.base.container.Folder#createProcess(com.yagasoft.overcast.base.container.Folder)
	 */
	@Override
	public DbxEntry.Folder createProcess(Folder<?> parent) throws CreationException
	{
		try
		{
			return Dropbox.dropboxService.createFolder(parent.getPath() + "/" + name);
		}
		catch (DbxException e)
		{
			throw new CreationException(e.getMessage());
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
	 * Builds the tree process.
	 *
	 * @param numberOfLevels
	 *            Number of levels.
	 * @param childrenArray
	 *            Children array.
	 * @throws OperationException
	 *             the operation exception
	 */
	@Override
	protected void buildTreeProcess(final int numberOfLevels, List<Container<?>> childrenArray) throws OperationException
	{
		try
		{
			// get folder list from Dropbox (metadata)
			DbxEntry.WithChildren listing = Dropbox.dropboxService.getMetadataWithChildren(path);
			
			// id and child
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
						childrenArray.add(Dropbox.factory.createFolder((DbxEntry.Folder) childAsEntry, false));
					}
					else
					{
						childrenArray.add(Dropbox.factory.createFile((DbxEntry.File) childAsEntry, false));
					}
				}
			}
		}
		catch (DbxException | CreationException e)
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
			id = getSourceObject().path;
			name = getSourceObject().name;
			if (name == null)
			{
				name = "";
			}
		}
		// size = calculateSize(); // commented because it might be heavy, so better do it explicitly.
		
		path = (((parent == null) || parent.getPath().equals("/")) ? "/" : (parent.getPath() + "/")) + name;
		cleanPath();
		
		notifyUpdateListeners();
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
	 * @see com.yagasoft.overcast.base.container.Container#copyProcess(com.yagasoft.overcast.base.container.Folder)
	 */
	@Override
	protected Container<?> copyProcess(Folder<?> destination)
			throws OperationException
	{
		try
		{
			return Dropbox.getFactory().createFolder(
					Dropbox.dropboxService.copy(path, destination.getPath() + "/" + name).asFolder(), false);
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
	protected DbxEntry.Folder moveProcess(Folder<?> destination)
			throws OperationException
	{
		try
		{
			return Dropbox.dropboxService.move(path, destination.getPath() + "/" + name).asFolder();
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
	protected DbxEntry.Folder renameProcess(String newName) throws OperationException
	{
		try
		{
			return Dropbox.dropboxService.move(path, getParent().getPath() + "/" + newName).asFolder();
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
