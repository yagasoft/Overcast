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

package com.yagasoft.overcast.ubuntu;


import java.net.MalformedURLException;
import java.net.URL;

import com.ubuntuone.api.files.model.U1File;
import com.ubuntuone.api.files.model.U1Node;
import com.ubuntuone.api.files.request.U1NodeListener;
import com.ubuntuone.api.files.util.U1Failure;
import com.ubuntuone.api.files.util.U1RequestListener.U1NodeRequestListener;
import com.yagasoft.overcast.container.Container;
import com.yagasoft.overcast.container.Folder;
import com.yagasoft.overcast.container.operation.IOperationListener;
import com.yagasoft.overcast.container.operation.Operation;
import com.yagasoft.overcast.container.operation.OperationState;
import com.yagasoft.overcast.exception.AccessException;
import com.yagasoft.overcast.exception.OperationException;


public class RemoteFile extends com.yagasoft.overcast.container.remote.RemoteFile<U1File>
{
	
	/**
	 * Better use the factory in Ubuntu class.
	 */
	public RemoteFile()
	{}
	
	/**
	 * @see com.yagasoft.overcast.container.Container#generateId()
	 */
	@Override
	public void generateId()
	{	
		
	}
	
	/**
	 * @see com.yagasoft.overcast.container.Container#isExist()
	 */
	@Override
	public boolean isExist() throws AccessException
	{
		return false;
	}
	
	/**
	 * @see com.yagasoft.overcast.container.Container#updateInfo()
	 */
	@Override
	public void updateInfo()
	{
		id = sourceObject.getKey();
		name = sourceObject.getName();
		path = sourceObject.getResourcePath();
		type = sourceObject.getKind().toString();
		
		try
		{
			size = sourceObject.size;
		}
		catch (Exception e)
		{
			size = 0;
		}
		
		try
		{
			link = new URL("https://files.one.ubuntu.com/" + sourceObject.getKey());
		}
		catch (MalformedURLException e)
		{
			link = null;
		}
	}
	
	/**
	 * @see com.yagasoft.overcast.container.Container#updateFromSource()
	 */
	@Override
	public void updateFromSource()
	{
		Ubuntu.ubuntuService.getNode((sourceObject == null) ? path : sourceObject.getResourcePath(), new U1NodeListener()
		{
			
			@Override
			public void onSuccess(U1Node node)
			{
				sourceObject = (U1File) node;
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
					Ubuntu.ubuntuService.deleteNode(path, new U1NodeRequestListener()
					{
						
						@Override
						public void onStart()
						{}
						
						@Override
						public void onSuccess(U1Node result)
						{
							parent.remove(RemoteFile.this);
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
						{
							clearOperationListeners(Operation.DELETE);
						}
					});
				}
				
			}).start();
		}
		catch (RuntimeException e)
		{
			throw new OperationException("Couldn't delete file.");
		}
		
	}
	
}
