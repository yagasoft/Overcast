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
			Container<?> result = getParent().searchByName(name, false)[0];

			path = getParent().getPath().toString() + "/" + name;

			// found something, and it's a folder.
			if (result != null && result.isFolder())
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
					setSourceObject((U1Directory) result);
					getParent().add(RemoteFolder.this);
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
		if ((numberOfLevels < 0) || !getSourceObject().hasChildren())
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

		try
		{
			for (String child : childrenIds)
			{
				U1Node childAsU1Node = childrenAsU1Node.get(child);

				if (childAsU1Node.getKind().toString().equals("directory"))
				{
					RemoteFolder folder = Ubuntu.factory.createFolder((U1Directory) childAsU1Node, false);
					add(folder);

					Logger.info("Folder: " + folder.getParent().getName() + "\\" + folder.name + " => " + folder.id);
				}
				else
				{
					RemoteFile file = Ubuntu.factory.createFile((U1File) childAsU1Node, false);
					add(file);

					Logger.info("File: " + name + "\\" + file.getName() + " => " + file.getId());
					Logger.info(file.getSourceObject().getPath());
				}
			}
		}
		catch (CreationException e)
		{
			Logger.error("uploading, can't create folder object");
			Logger.except(e);
			e.printStackTrace();

			throw new OperationException("Can't create folder object!");
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
	 * @see com.yagasoft.overcast.base.container.Folder#updateInfo()
	 */
	@Override
	public synchronized void updateInfo()
	{
		super.updateInfo();

		id = getSourceObject().getKey();
		name = getSourceObject().getName();
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

		if (folderContents)
		{
			buildTree(false);
		}

		Ubuntu.ubuntuService.getNode((getSourceObject() == null) ? getUbuntuPath() : getSourceObject().getResourcePath(),
				new U1NodeListener()
				{

					@Override
					public void onSuccess(U1Node node)
					{
						setSourceObject((U1Directory) node);
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
		return null;
	}

	/**
	 * @see com.yagasoft.overcast.base.container.Container#move(com.yagasoft.overcast.base.container.Folder, boolean,
	 *      IOperationListener)
	 */
	@Override
	public synchronized void move(Folder<?> destination, boolean overwrite, IOperationListener listener)
			throws OperationException
	{}

	/**
	 * @see com.yagasoft.overcast.base.container.Container#rename(java.lang.String, IOperationListener)
	 */
	@Override
	public synchronized void rename(String newName, IOperationListener listener) throws OperationException
	{
		addOperationListener(listener, Operation.RENAME);

		Container<?> existingFolder = getParent().searchByName(newName, false)[0];

		// found something, and it's a folder.
		if ((existingFolder != null) && existingFolder.isFolder())
		{
			notifyOperationListeners(Operation.RENAME, OperationState.FAILED, 0.0f);
			clearOperationListeners(Operation.RENAME);
			throw new OperationException("Folder already exists.");
		}

		try
		{
			Ubuntu.ubuntuService.moveNode(getUbuntuPath(),
					((com.yagasoft.overcast.implement.ubuntu.RemoteFolder) getParent()).getUbuntuPath() + "/" + newName,
					new U1NodeRequestListener()
					{

						@Override
						public void onStart()
						{}

						@Override
						public void onSuccess(U1Node result)
						{
							setSourceObject((U1Directory) result);
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
					getParent().remove(RemoteFolder.this);
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
