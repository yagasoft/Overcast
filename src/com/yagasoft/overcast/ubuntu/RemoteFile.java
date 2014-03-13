
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
import com.yagasoft.overcast.exception.AccessException;
import com.yagasoft.overcast.exception.OperationException;


public class RemoteFile extends com.yagasoft.overcast.container.remote.RemoteFile<U1File>
{

	private boolean	operationSuccess	= false;

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
	 * @see com.yagasoft.overcast.container.Container#copy(com.yagasoft.overcast.container.Folder, boolean)
	 */
	@Override
	public Container<?> copy(Folder<?> destination, boolean overwrite) throws OperationException
	{
		return null;
	}

	/**
	 * @see com.yagasoft.overcast.container.Container#move(com.yagasoft.overcast.container.Folder, boolean)
	 */
	@Override
	public void move(Folder<?> destination, boolean overwrite) throws OperationException
	{}

	/**
	 * @see com.yagasoft.overcast.container.Container#rename(java.lang.String)
	 */
	@Override
	public void rename(String newName) throws OperationException
	{}

	/**
	 * @see com.yagasoft.overcast.container.Container#delete()
	 */
	@Override
	public void delete() throws OperationException
	{
		operationSuccess = false;
		
		Ubuntu.ubuntuService.deleteNode(path, new U1NodeRequestListener()
		{

			@Override
			public void onStart()
			{}

			@Override
			public void onSuccess(U1Node result)
			{
				operationSuccess = true;
			}

			@Override
			public void onUbuntuOneFailure(U1Failure failure)
			{}

			@Override
			public void onFailure(U1Failure failure)
			{}

			@Override
			public void onFinish()
			{}
		});

		if ( !operationSuccess)
		{
			throw new OperationException("Failed to delete file.");
		}
		else
		{
			operationSuccess = false;
			parent.remove(this);
		}
	}

}
