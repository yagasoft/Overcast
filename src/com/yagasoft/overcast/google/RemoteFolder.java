/* 
 * Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
 * 
 *		Modified MIT License (GPL v3 compatible)
 * 			License terms are in a separate file (license.txt)
 * 
 *		Project/File: Overcast/com.yagasoft.overcast.google/RemoteFolder.java
 * 
 *			Modified: 27-Mar-2014 (16:15:08)
 *			   Using: Eclipse J-EE / JDK 7 / Windows 8.1 x64
 */

package com.yagasoft.overcast.google;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.Drive.Files.List;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.ParentReference;
import com.yagasoft.logger.Logger;
import com.yagasoft.overcast.container.Container;
import com.yagasoft.overcast.container.Folder;
import com.yagasoft.overcast.container.operation.IOperationListener;
import com.yagasoft.overcast.container.operation.Operation;
import com.yagasoft.overcast.container.operation.OperationState;
import com.yagasoft.overcast.exception.CreationException;
import com.yagasoft.overcast.exception.OperationException;


public class RemoteFolder extends com.yagasoft.overcast.container.remote.RemoteFolder<File>
{
	
	/**
	 * Better use the factory in Google class.
	 */
	public RemoteFolder()
	{}
	
	/**
	 * @see com.yagasoft.overcast.container.Container#generateId()
	 */
	@Override
	public void generateId()
	{	
		
	}
	
	/**
	 * @see com.yagasoft.overcast.container.Folder#create(com.yagasoft.overcast.container.Folder, IOperationListener)
	 */
	@Override
	public void create(final Folder<?> parent, IOperationListener listener) throws CreationException
	{
		addOperationListener(listener, Operation.CREATE);
		
		try
		{
			new Thread(new Runnable()
			{
				
				@Override
				public void run()
				{
					RemoteFolder result = parent.searchByName(name, false);
					
					if (result != null)
					{
						notifyOperationListeners(Operation.CREATE, OperationState.FAILED, 0f);
						throw new RuntimeException("Folder already Exists!");
					}
					
					File metadata = new File();
					metadata.setTitle(name);
					metadata.setMimeType("application/vnd.google-apps.folder");
					metadata.setParents(Arrays.asList(new ParentReference().setId(parent.getId())));
					
					try
					{
						Drive.Files.Insert insert = Google.driveService.files().insert(metadata);
						sourceObject = insert.execute();
						updateInfo();
						parent.add(RemoteFolder.this);
						notifyOperationListeners(Operation.CREATE, OperationState.COMPLETED, 1.0f);
					}
					catch (IOException e)
					{
						e.printStackTrace();
						notifyOperationListeners(Operation.CREATE, OperationState.FAILED, 0f);
						throw new RuntimeException("Couldn't create folder.");
					}
					
					clearOperationListeners(Operation.CREATE);
				}
			}).start();
		}
		catch (RuntimeException e)
		{
			throw new CreationException(e.getMessage());
		}
	}
	
	/**
	 * @see com.yagasoft.overcast.container.Folder#create(java.lang.String, IOperationListener)
	 */
	@Override
	public void create(String parentPath, IOperationListener listener) throws CreationException
	{
		ArrayList<String> splitPath = new ArrayList<String>(Arrays.asList(path.split("/")));
		
		if (splitPath.get(0).equals(""))
		{
			splitPath.remove(0);
		}
		
		RemoteFolder parent = (RemoteFolder) csp.getRemoteFileTree();
		
		if (splitPath.size() > 0)
		{
			RemoteFolder result = parent.searchByName(splitPath.get(0), false);
			
			while ((result != null) && (splitPath.size() > 0))
			{
				parent = result;
				splitPath.remove(0);
				
				if (splitPath.size() > 0)
				{
					result = parent.searchByName(splitPath.get(0), false);
				}
				else
				{
					break;
				}
			}
		}
		
		while (splitPath.size() > 0)
		{
			RemoteFolder tempFolder = (RemoteFolder) csp.getAbstractFactory().createFolder();
			tempFolder.setName(splitPath.remove(0));
			tempFolder.create(parent, listener);
			
			parent = tempFolder;
		}
		
		RemoteFolder result = parent.searchByName(name, false);
		
		if (result != null)
		{
			notifyOperationListeners(Operation.CREATE, OperationState.FAILED, 0f);
			throw new CreationException("Folder already exists!");
		}
		else
		{
			create(parent, listener);
		}
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
	 * @see com.yagasoft.overcast.container.Folder#buildTree(int)
	 */
	@Override
	public void buildTree(final int numberOfLevels)
	{
		if (numberOfLevels < 0)
		{
			return;
		}
		
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
					System.out.println("An error occurred: " + e);
					request.setPageToken(null);
				}
			} while ((request.getPageToken() != null) && (request.getPageToken().length() > 0));
			
			removeObsolete(childrenIds, true);
			
			if ( !childrenIds.isEmpty())
			{
				for (String id1 : childrenIds)
				{
					File remote = children.get(id1);
					if (remote.getMimeType().indexOf("folder") >= 0)
					{
						RemoteFolder folder = Google.factory.createFolder(remote, false);
						add(folder);
						
						Logger.newEntry("Folder: " + folder.parent.getName() + "\\" + folder.name + " => " + folder.id);
					}
					else
					{
						RemoteFile file = Google.factory.createFile(remote, false);
						add(file);
						
						Logger.newEntry("File: " + name + "\\" + file.getName() + " => " + file.getId());
					}
				}
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		for (Folder<?> folder : getFoldersArray())
		{
			folder.buildTree(numberOfLevels - 1);
		}
	}
	
	/**
	 * @see com.yagasoft.overcast.container.Folder#calculateSize()
	 */
	@Override
	public long calculateSize()
	{
		return 0;
	}
	
	/**
	 * @see com.yagasoft.overcast.container.Folder#updateInfo(boolean, boolean)
	 */
	@Override
	public void updateInfo(boolean folderContents, boolean recursively)
	{
		id = sourceObject.getId();
		name = sourceObject.getTitle();
		path = ((parent == null) ? "/" : (parent.getPath())) + name + "/";
		
//		try
//		{
//			link= new URL(sourceObject.getDownloadUrl());
//		}
//		catch (MalformedURLException e)
//		{
//			e.printStackTrace();
//		}
	}
	
	/**
	 * @see com.yagasoft.overcast.container.Folder#updateFromSource(boolean, boolean)
	 */
	@Override
	public void updateFromSource(boolean folderContents, final boolean recursively)
	{
		if (folderContents)
		{
			buildTree(recursively);
		}
		
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
						Drive.Files.Delete delete = Google.driveService.files().delete(id);
						delete.execute();
						parent.remove(RemoteFolder.this);
						notifyOperationListeners(Operation.DELETE, OperationState.COMPLETED, 1.0f);
					}
					catch (IOException e)
					{
						e.printStackTrace();
						notifyOperationListeners(Operation.DELETE, OperationState.FAILED, 0f);
						throw new RuntimeException("Couldn't delete folder.");
					}
					
					clearOperationListeners(Operation.DELETE);
				}
			}).start();
		}
		catch (RuntimeException e)
		{
			throw new OperationException(e.getMessage());
		}
	}
	
	/**
	 * @see com.yagasoft.overcast.container.Container#updateInfo()
	 */
	@Override
	public void updateInfo()
	{
		updateInfo(false, false);
	}
	
	/**
	 * @see com.yagasoft.overcast.container.Container#updateFromSource()
	 */
	@Override
	public void updateFromSource()
	{
		updateFromSource(false, false);
	}
	
}
