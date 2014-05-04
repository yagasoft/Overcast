/*
 * Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
 *
 *		Modified MIT License (GPL v3 compatible)
 * 			License terms are in a separate file (license.txt)
 *
 *		Project/File: Overcast/com.yagasoft.overcast.ubuntu/RemoteFile.java
 *
 *			Modified: 27-Mar-2014 (16:15:37)
 *			   Using: Eclipse J-EE / JDK 7 / Windows 8.1 x64
 */

package com.yagasoft.overcast.implement.ubuntu;


import java.net.MalformedURLException;
import java.net.URL;

import com.ubuntuone.api.files.model.U1File;
import com.ubuntuone.api.files.model.U1Node;
import com.ubuntuone.api.files.request.U1NodeListener;
import com.ubuntuone.api.files.util.U1Failure;
import com.ubuntuone.api.files.util.U1RequestListener.U1NodeRequestListener;
import com.yagasoft.overcast.base.container.Container;
import com.yagasoft.overcast.base.container.Folder;
import com.yagasoft.overcast.base.container.operation.IOperationListener;
import com.yagasoft.overcast.base.container.operation.Operation;
import com.yagasoft.overcast.base.container.operation.OperationState;
import com.yagasoft.overcast.base.container.remote.RemoteFolder;
import com.yagasoft.overcast.base.container.transfer.ITransferProgressListener;
import com.yagasoft.overcast.base.container.transfer.TransferEvent;
import com.yagasoft.overcast.base.container.transfer.TransferState;
import com.yagasoft.overcast.exception.AccessException;
import com.yagasoft.overcast.exception.OperationException;
import com.yagasoft.overcast.exception.TransferException;


public class RemoteFile extends com.yagasoft.overcast.base.container.remote.RemoteFile<U1File>
{

	/**
	 * Better use the factory in Ubuntu class.
	 */
	public RemoteFile()
	{}

	/**
	 * @see com.yagasoft.overcast.base.container.Container#generateId()
	 */
	@Override
	public void generateId()
	{

	}

	/**
	 * @see com.yagasoft.overcast.base.container.Container#isExist()
	 */
	@Override
	public synchronized boolean isExist() throws AccessException
	{
		return false;
	}

	/**
	 * @see com.yagasoft.overcast.base.container.Container#updateInfo()
	 */
	@Override
	public synchronized void updateInfo()
	{
		super.updateInfo();

		id = getSourceObject().getKey();
		name = getSourceObject().getName();
		type = getSourceObject().getKind().toString();

		try
		{
			size = getSourceObject().size;
		}
		catch (Exception e)
		{
			size = 0;
		}

		try
		{
			link = new URL("https://files.one.ubuntu.com/" + getSourceObject().getKey());
		}
		catch (MalformedURLException e)
		{
			link = null;
		}
	}

	/**
	 * @see com.yagasoft.overcast.base.container.Container#updateFromSource()
	 */
	@Override
	public synchronized void updateFromSource()
	{
		Ubuntu.ubuntuService.getNode((getSourceObject() == null) ? getUbuntuPath() : getSourceObject().getResourcePath(), new U1NodeListener()
		{

			@Override
			public void onSuccess(U1Node node)
			{
				setSourceObject((U1File) node);
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
	 * @see com.yagasoft.overcast.base.container.Container#copy(com.yagasoft.overcast.base.container.Folder, boolean, IOperationListener)
	 */
	@Override
	public synchronized Container<?> copy(Folder<?> destination, boolean overwrite, IOperationListener listener) throws OperationException
	{
		addOperationListener(listener, Operation.COPY);

//		Container<?> existingFile = destination.searchByName(name, false);
//
//		if (existingFile != null && existingFile instanceof RemoteFile)
//		{
//			if (overwrite)
//			{
//				existingFile.delete(new IOperationListener()
//				{
//
//					@Override
//					public void operationProgressChanged(OperationEvent event)
//					{}
//				});
//			}
//			else
//			{
//				notifyOperationListeners(Operation.COPY, OperationState.FAILED, 0.0f);
//				throw new OperationException("File already exists.");
//			}
//		}
//
//		try
//		{
//			if (localMapping.isExist())
//			{
//				localMapping.upload((RemoteFolder<?>) destination, false, new ITransferProgressListener()
//				{
//
//					@Override
//					public void transferProgressChanged(TransferEvent event)
//					{}
//				});
//			}
//		}
//		catch (AccessException | TransferException e)
//		{
//			e.printStackTrace();
//			notifyOperationListeners(Operation.COPY, OperationState.FAILED, 0.0f);
//			throw new OperationException("Couldn't copy file.");
//		}

		try
		{
			if (localMapping != null)
			{
				return localMapping.upload((RemoteFolder<?>) destination, overwrite, new ITransferProgressListener()
				{

					@Override
					public void transferProgressChanged(TransferEvent event)
					{
						if (event.getState() == TransferState.COMPLETED)
						{
							notifyOperationListeners(Operation.COPY, OperationState.COMPLETED, 1.0f);
						}
					}
				}).getRemoteFile();
			}
			else
			{
				notifyOperationListeners(Operation.COPY, OperationState.FAILED, 0.0f);
				throw new OperationException("Ubuntu doesn't support copy.");
			}
		}
		catch (TransferException e)
		{
			e.printStackTrace();
			notifyOperationListeners(Operation.COPY, OperationState.FAILED, 0.0f);
			throw new OperationException("Ubuntu doesn't support copy.");
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
	public synchronized void move(final Folder<?> destination, boolean overwrite, IOperationListener listener) throws OperationException
	{
		addOperationListener(listener, Operation.MOVE);

		try
		{
			if (localMapping != null)
			{
				localMapping.upload((RemoteFolder<?>) destination, overwrite, new ITransferProgressListener()
				{

					@Override
					public void transferProgressChanged(TransferEvent event)
					{
						if (event.getState() == TransferState.COMPLETED)
						{
							notifyOperationListeners(Operation.MOVE, OperationState.COMPLETED, 1.0f);
							RemoteFile.this.getParent().remove(RemoteFile.this);
						}
					}
				}).getRemoteFile();
			}
			else
			{
				notifyOperationListeners(Operation.MOVE, OperationState.FAILED, 0.0f);
				throw new OperationException("Ubuntu doesn't support move.");
			}
		}
		catch (TransferException e)
		{
			e.printStackTrace();
			notifyOperationListeners(Operation.MOVE, OperationState.FAILED, 0.0f);
			throw new OperationException("Ubuntu doesn't support move.");
		}
		finally
		{
			clearOperationListeners(Operation.MOVE);
		}

//		Container<?> existingFile = destination.searchByName(name, false);
//
//		if (existingFile != null && existingFile instanceof RemoteFile)
//		{
//			if (overwrite)
//			{
//				try
//				{
//					existingFile.delete(new IOperationListener()
//					{
//
//						@Override
//						public void operationProgressChanged(OperationEvent event)
//						{}
//					});
//				}
//				catch (OperationException e)
//				{
//					e.printStackTrace();
//					clearOperationListeners(Operation.MOVE);
//					throw new OperationException(e.getMessage());
//				}
//			}
//			else
//			{
//				notifyOperationListeners(Operation.MOVE, OperationState.FAILED, 0.0f);
//				clearOperationListeners(Operation.MOVE);
//				throw new OperationException("File already exists.");
//			}
//		}
//
//		try
//		{
//			Ubuntu.ubuntuService.moveNode(getUbuntuPath(), ((com.yagasoft.overcast.ubuntu.RemoteFolder) destination).getUbuntuPath() + "/", new U1NodeRequestListener()
//			{
//
//				@Override
//				public void onStart()
//				{
//					System.out.println(getUbuntuPath());
//					System.out.println(((com.yagasoft.overcast.ubuntu.RemoteFolder) destination).getUbuntuPath() + "/" + name);
//				}
//
//				@Override
//				public void onSuccess(U1Node result)
//				{
//					setSourceObject((U1File) result;
//					updateInfo();
//					notifyOperationListeners(Operation.MOVE, OperationState.COMPLETED, 1.0f);
//				}
//
//				@Override
//				public void onFailure(U1Failure failure)
//				{
//					notifyOperationListeners(Operation.MOVE, OperationState.FAILED, 0.0f);
//					throw new RuntimeException("Couldn't move file.");
//				}
//
//				@Override
//				public void onUbuntuOneFailure(U1Failure failure)
//				{
//					notifyOperationListeners(Operation.MOVE, OperationState.FAILED, 0.0f);
//					throw new RuntimeException("Couldn't move file.");
//				}
//
//				@Override
//				public void onFinish()
//				{}
//			});
//		}
//		catch (RuntimeException e)
//		{
//			e.printStackTrace();
//			throw new OperationException(e.getMessage());
//		}
//		finally
//		{
//			clearOperationListeners(Operation.MOVE);
//		}
	}

	/**
	 * @see com.yagasoft.overcast.base.container.Container#rename(java.lang.String, IOperationListener)
	 */
	@Override
	public synchronized void rename(String newName, IOperationListener listener) throws OperationException
	{
		addOperationListener(listener, Operation.RENAME);

		notifyOperationListeners(Operation.RENAME, OperationState.FAILED, 0.0f);
		clearOperationListeners(Operation.RENAME);

		throw new OperationException("Ubuntu doesn't support rename.");

//		Container<?> existingFile = getParent().searchByName(newName, false);
//
//		if (existingFile != null && existingFile instanceof RemoteFile)
//		{
//			notifyOperationListeners(Operation.RENAME, OperationState.FAILED, 0.0f);
//			clearOperationListeners(Operation.RENAME);
//			throw new OperationException("File already exists.");
//		}
//
//		try
//		{
//			Ubuntu.ubuntuService.moveNode(getUbuntuPath(), ((com.yagasoft.overcast.ubuntu.RemoteFolder) parent).getUbuntuPath() + "/" + newName, new U1NodeRequestListener()
//			{
//
//				@Override
//				public void onStart()
//				{}
//
//				@Override
//				public void onSuccess(U1Node result)
//				{
//					setSourceObject((U1File) result;
//					updateInfo();
//					notifyOperationListeners(Operation.RENAME, OperationState.COMPLETED, 1.0f);
//				}
//
//				@Override
//				public void onFailure(U1Failure failure)
//				{
//					notifyOperationListeners(Operation.RENAME, OperationState.FAILED, 0.0f);
//					throw new RuntimeException("Couldn't rename file.");
//				}
//
//				@Override
//				public void onUbuntuOneFailure(U1Failure failure)
//				{
//					notifyOperationListeners(Operation.RENAME, OperationState.FAILED, 0.0f);
//					throw new RuntimeException("Couldn't rename file.");
//				}
//
//				@Override
//				public void onFinish()
//				{}
//			});
//		}
//		catch (RuntimeException e)
//		{
//			e.printStackTrace();
//			throw new OperationException(e.getMessage());
//		}
//		finally
//		{
//			clearOperationListeners(Operation.RENAME);
//		}
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
					getParent().remove(RemoteFile.this);
					notifyOperationListeners(Operation.DELETE, OperationState.COMPLETED, 1.0f);
				}

				@Override
				public void onUbuntuOneFailure(U1Failure failure)
				{
					notifyOperationListeners(Operation.DELETE, OperationState.FAILED, 0.0f);
					throw new RuntimeException("Couldn't delete file.");
				}

				@Override
				public void onFailure(U1Failure failure)
				{
					notifyOperationListeners(Operation.DELETE, OperationState.FAILED, 0.0f);
					throw new RuntimeException("Couldn't delete file.");
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
			clearOperationListeners(Operation.DELETE);
		}
	}

	public String getUbuntuPath()
	{
		return "/~/Ubuntu One" + path;
	}
}
