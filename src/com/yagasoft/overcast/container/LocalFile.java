
package com.yagasoft.overcast.container;


import static java.nio.file.StandardCopyOption.COPY_ATTRIBUTES;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.io.IOException;
import java.net.URLConnection;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.yagasoft.overcast.CSP;


public class LocalFile extends File<Path> implements ILocal
{
	
	protected RemoteFile<?>	remoteMapping;
	@SuppressWarnings("unused")
	private CSP				csp	= null;
	
	/**
	 * Instantiates a new local file.
	 */
	public LocalFile()
	{}
	
	/**
	 * Instantiates a new local file.
	 * 
	 * @param file
	 *            Java library File object.
	 */
	public LocalFile(Path file)
	{
		sourceObject = file;
		updateInfo();
	}
	
	/**
	 * Instantiates a new local file.
	 * 
	 * @param path
	 *            Path to the file.
	 */
	public LocalFile(String path)
	{
		this(Paths.get(path));
	}
	
	/**
	 * @see com.yagasoft.overcast.container.Container#isExist()
	 */
	@Override
	public boolean isExist() throws Exception
	{
		if ( !Files.exists(sourceObject) && Files.notExists(sourceObject))
		{
			throw new Exception("Can't determine if file exists or not.");
		}
		
		return Files.exists(sourceObject);
	}
	
	/**
	 * @see com.yagasoft.overcast.container.Container#updateInfo()
	 */
	@Override
	public void updateInfo()
	{
		updateFromSource();
	}
	
	/**
	 * @see com.yagasoft.overcast.container.Container#updateFromSource()
	 */
	@Override
	public void updateFromSource()
	{
		name = sourceObject.getFileName().toString();
		path = sourceObject.toAbsolutePath().toString();
		type = URLConnection.guessContentTypeFromName(path);
		
		try
		{
			size = Files.size(sourceObject);
		}
		catch (IOException e)
		{
			size = 0;
			e.printStackTrace();
		}
		
		generateId();
	}
	
	/**
	 * @see com.yagasoft.overcast.container.Container#copy(com.yagasoft.overcast.container.Folder,
	 *      boolean)
	 */
	@Override
	public LocalFile copy(Folder<?> destination, boolean overwrite)
	{
		try
		{
			return new LocalFile(Files.copy(sourceObject, ((Path) destination.sourceObject).resolve(sourceObject.getFileName())
					, overwrite ?
							new CopyOption[] { REPLACE_EXISTING, COPY_ATTRIBUTES }
							:
							new CopyOption[] { COPY_ATTRIBUTES }));
			
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * @see com.yagasoft.overcast.container.Container#move(com.yagasoft.overcast.container.Folder,
	 *      boolean)
	 */
	@Override
	public void move(Folder<?> destination, boolean overwrite)
	{
		try
		{
			sourceObject = Files.move(sourceObject, ((Path) destination.sourceObject).resolve(sourceObject.getFileName())
					, overwrite ?
							new CopyOption[] { REPLACE_EXISTING }
							:
							new CopyOption[0]);
			
			updateFromSource();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * @see com.yagasoft.overcast.container.Container#rename(java.lang.String)
	 */
	@Override
	public void rename(String newName)
	{
		try
		{
			sourceObject = Files.move(sourceObject, sourceObject.resolveSibling(newName));
			updateFromSource();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * @see com.yagasoft.overcast.container.Container#delete()
	 */
	@Override
	public void delete()
	{
		try
		{
			Files.deleteIfExists(sourceObject);
			
			if (parent != null)
			{
				parent.remove(this);
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * @return the remoteMapping
	 */
	public RemoteFile<?> getRemoteMapping()
	{
		return remoteMapping;
	}
	
	/**
	 * @param remoteMapping
	 *            the remoteMapping to set
	 */
	public void setRemoteMapping(RemoteFile<?> remoteMapping)
	{
		this.remoteMapping = remoteMapping;
	}
	
	/**
	 * @return the csp
	 */
	@Override
	public CSP getCsp()
	{
		throw new UnsupportedOperationException();
	}
	
	/**
	 * @param csp
	 *            the csp to set
	 */
	@Override
	public void setCsp(CSP csp)
	{
		throw new UnsupportedOperationException();
	}
	
}
