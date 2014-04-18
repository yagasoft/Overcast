/*
 * Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
 *
 *		Modified MIT License (GPL v3 compatible)
 * 			License terms are in a separate file (license.txt)
 *
 *		Project/File: Overcast/com.yagasoft.overcast.ubuntu/RemoteFolder.java
 *
 *			Modified: 27-Mar-2014 (16:15:42)
 *			   Using: Eclipse J-EE / JDK 7 / Windows 8.1 x64
 */

package com.yagasoft.overcast.implement.ubuntu;


import java.util.ArrayList;
import java.util.HashMap;

import com.ubuntuone.api.files.model.U1Directory;
import com.ubuntuone.api.files.model.U1File;
import com.ubuntuone.api.files.model.U1Node;
import com.ubuntuone.api.files.request.U1NodeListener;
import com.ubuntuone.api.files.util.U1Failure;
import com.ubuntuone.api.files.util.U1RequestListener.U1NodeRequestListener;
import com.yagasoft.logger.Logger;
import com.yagasoft.overcast.base.container.Container;
import com.yagasoft.overcast.base.container.Folder;
import com.yagasoft.overcast.base.container.operation.IOperationListener;
import com.yagasoft.overcast.base.container.operation.Operation;
import com.yagasoft.overcast.base.container.operation.OperationState;
import com.yagasoft.overcast.exception.AccessException;
import com.yagasoft.overcast.exception.CreationException;
import com.yagasoft.overcast.exception.OperationException;


public class RemoteFolder extends com.yagasoft.overcast.base.container.remote.RemoteFolder<U1Directory>
{

	/**
	 * Better use the factory in Ubuntu class.
	 */
	public RemoteFolder()
	{}

	/**
	 * @see com.yagasoft.overcast.base.container.Container#generateId()
	 */
	@Override
	public void generateId()
	{

	}

	/**
	 * @see com.yagasoft.overcast.base.container.Folder#create(com.yagasoft.overcast.base.container.Folder)
	 */
	@Override
	public synchronized void create(final Folder<?> parent, IOperationListener listener) throws CreationException
	{
		addOperationListener(listener, Operation.CREATE);

		try
		{
			RemoteFolder result = parent.searchByName(name, false);

			path = parent.getPath().toString() + "/" + name;

			if (result != null)
			{
				notifyOperationListeners(Operation.CREATE, OperationState.FAILED, 0f);
				throw new RuntimeException("Folder already Exists!");
			}

			Ubuntu.ubuntuService.makeDirectory(getUbuntuPath(), new U1NodeRequestListener()
			{

				@Override
				public void onStart()
				{}

				@Override
				public void onSuccess(U1Node result)
				{
					sourceObject = (U1Directory) result;
					updateInfo();
					parent.add(RemoteFolder.this);
					notifyOperationListeners(Operation.CREATE, OperationState.COMPLETED, 1.0f);
				}

				@Override
				public void onUbuntuOneFailure(U1Failure failure)
				{
					notifyOperationListeners(Operation.CREATE, OperationState.FAILED, 0.0f);
					throw new RuntimeException("Couldn't delete file.");
				}

				@Override
				public void onFailure(U1Failure failure)
				{
					notifyOperationListeners(Operation.CREATE, OperationState.FAILED, 0.0f);
					throw new RuntimeException("Couldn't delete file.");
				}

				@Override
				public void onFinish()
				{}
			});
		}
		catch (RuntimeException e)
		{
			throw new CreationException(e.getMessage());
		}
		finally
		{
			clearOperationListeners(Operation.CREATE);
		}
	}

	/**
	 * @see com.yagasoft.overcast.base.container.Folder#create(java.lang.String)
	 */
	@Override
	public synchronized void create(String parentPath, IOperationListener listener) throws CreationException
	{}

	/**
	 * @see com.yagasoft.overcast.base.container.Container#isExist()
	 */
	@Override
	public synchronized boolean isExist() throws AccessException
	{
		return false;
	}

	/**
	 * @see com.yagasoft.overcast.base.container.Folder#buildTree(int)
	 */
	@Override
	public synchronized void buildTree(int numberOfLevels) throws OperationException
	{
		if ((numberOfLevels < 0) || !sourceObject.hasChildren())
		{
			return;
		}

		final HashMap<String, U1Node> childrenAsU1Node = new HashMap<String, U1Node>();

		Ubuntu.ubuntuService.listDirectory(getUbuntuPath(), new U1NodeListener()
		{

			@Override
			public void onSuccess(U1Node result)
			{
				childrenAsU1Node.put(result.getKey(), result);
			}

			@Override
			public void onUbuntuOneFailure(U1Failure failure)
			{
				System.err.println("Ubuntu One failure: " + failure);
			}

			@Override
			public void onFailure(U1Failure failure)
			{
				System.err.println("General failure: " + failure);
			}
		});

		ArrayList<String> childrenIds = new ArrayList<String>(childrenAsU1Node.keySet());

		removeObsolete(childrenIds, true);

		for (String child : childrenIds)
		{
			U1Node childAsU1Node = childrenAsU1Node.get(child);

			if (childAsU1Node.getKind().toString().equals("directory"))
			{
				RemoteFolder folder = Ubuntu.factory.createFolder((U1Directory) childAsU1Node, false);
				add(folder);

				Logger.info("Folder: " + folder.parent.getName() + "\\" + folder.name + " => " + folder.id);
			}
			else
			{
				RemoteFile file = Ubuntu.factory.createFile((U1File) childAsU1Node, false);
				add(file);

				Logger.info("File: " + name + "\\" + file.getName() + " => " + file.getId());
				Logger.info(file.getSourceObject().getPath());
			}
		}

		for (Folder<?> folder : getFoldersArray())
		{
			folder.buildTree(numberOfLevels - 1);
		}
	}

	/**
	 * @see com.yagasoft.overcast.base.container.Folder#calculateSize()
	 */
	@Override
	public synchronized long calculateSize()
	{
		return 0;
	}

	/**
	 * @see com.yagasoft.overcast.base.container.Folder#updateInfo(boolean, boolean)
	 */
	@Override
	public synchronized void updateInfo(boolean folderContents, boolean recursively)
	{
		id = sourceObject.getKey();
		name = sourceObject.getName();
		path = sourceObject.getResourcePath().replace("/~/Ubuntu One", "");
	}

	/**
	 * @see com.yagasoft.overcast.base.container.Folder#updateFromSource(boolean, boolean)
	 */
	@Override
	public synchronized void updateFromSource(boolean folderContents, boolean recursively) throws OperationException
	{
		if (folderContents)
		{
			buildTree(recursively);
		}

		Ubuntu.ubuntuService.getNode((sourceObject == null) ? getUbuntuPath() : sourceObject.getResourcePath(), new U1NodeListener()
		{

			@Override
			public void onSuccess(U1Node node)
			{
				sourceObject = (U1Directory) node;
			}

			@Override
			public void onUbuntuOneFailure(U1Failure failure)
			{
				System.err.println("Ubuntu One error: " + failure.getMessage());
			}

			@Override
			public void onFailure(U1Failure failure)
			{
				System.err.println("General error: " + failure.getMessage());
			}
		});

		updateInfo();
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

	/**
	 * @see com.yagasoft.overcast.base.container.Container#copy(com.yagasoft.overcast.base.container.Folder, boolean, IOperationListener)
	 */
	@Override
	public synchronized Container<?> copy(Folder<?> destination, boolean overwrite, IOperationListener listener) throws OperationException
	{
		return null;
	}

	/**
	 * @see com.yagasoft.overcast.base.container.Container#move(com.yagasoft.overcast.base.container.Folder, boolean, IOperationListener)
	 */
	@Override
	public synchronized void move(Folder<?> destination, boolean overwrite, IOperationListener listener) throws OperationException
	{}

	/**
	 * @see com.yagasoft.overcast.base.container.Container#rename(java.lang.String, IOperationListener)
	 */
	@Override
	public synchronized void rename(String newName, IOperationListener listener) throws OperationException
	{
		addOperationListener(listener, Operation.RENAME);

		Container<?> existingFile = parent.searchByName(newName, false);

		if (existingFile != null && existingFile instanceof RemoteFolder)
		{
			notifyOperationListeners(Operation.RENAME, OperationState.FAILED, 0.0f);
			clearOperationListeners(Operation.RENAME);
			throw new OperationException("Folder already exists.");
		}

		try
		{
			Ubuntu.ubuntuService.moveNode(getUbuntuPath(), ((com.yagasoft.overcast.implement.ubuntu.RemoteFolder) parent).getUbuntuPath() + "/" + newName, new U1NodeRequestListener()
			{

				@Override
				public void onStart()
				{}

				@Override
				public void onSuccess(U1Node result)
				{
					sourceObject = (U1Directory) result;
					updateInfo();
					notifyOperationListeners(Operation.RENAME, OperationState.COMPLETED, 1.0f);
				}

				@Override
				public void onFailure(U1Failure failure)
				{
					notifyOperationListeners(Operation.RENAME, OperationState.FAILED, 0.0f);
					throw new RuntimeException("Couldn't rename folder.");
				}

				@Override
				public void onUbuntuOneFailure(U1Failure failure)
				{
					notifyOperationListeners(Operation.RENAME, OperationState.FAILED, 0.0f);
					throw new RuntimeException("Couldn't rename folder.");
				}

				@Override
				public void onFinish()
				{}
			});
		}
		catch (RuntimeException e)
		{
			e.printStackTrace();
			throw new OperationException(e.getMessage());
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
		addOperationListener(listener, Operation.DELETE);

		try
		{
			Ubuntu.ubuntuService.deleteNode(getUbuntuPath(), new U1NodeRequestListener()
			{

				@Override
				public void onStart()
				{}

				@Override
				public void onSuccess(U1Node result)
				{
					parent.remove(RemoteFolder.this);
					notifyOperationListeners(Operation.DELETE, OperationState.COMPLETED, 1.0f);
				}

				@Override
				public void onFailure(U1Failure failure)
				{
					notifyOperationListeners(Operation.DELETE, OperationState.FAILED, 0.0f);
					throw new RuntimeException("Couldn't delete folder.");
				}

				@Override
				public void onUbuntuOneFailure(U1Failure failure)
				{
					notifyOperationListeners(Operation.DELETE, OperationState.FAILED, 0.0f);
					throw new RuntimeException("Couldn't delete folder.");
				}

				@Override
				public void onFinish()
				{}
			});
		}
		catch (RuntimeException e)
		{
			throw new OperationException(e.getMessage());
		}
		finally
		{
			clearOperationListeners(Operation.DELETE);
		}
	}

	public String getUbuntuPath()
	{
		return "/~/Ubuntu One" + path;
	}

}

