
package com.yagasoft.overcast;


import com.yagasoft.overcast.container.LocalFile;
import com.yagasoft.overcast.container.RemoteFolder;


public final class UploadJob<T>
{
	
	LocalFile		file;
	RemoteFolder<?>	parent;
	boolean			overwrite;
	T				cspUploader;
	
	public UploadJob(LocalFile file, RemoteFolder<?> parent, boolean overwrite, T cspUploader)
	{
		this.file = file;
		this.parent = parent;
		this.overwrite = overwrite;
		this.cspUploader = cspUploader;
	}
	
	// --------------------------------------------------------------------------------------
	// #region Getters and setters.
	
	/**
	 * @return the file
	 */
	public LocalFile getFile()
	{
		return file;
	}
	
	/**
	 * @param file
	 *            the file to set
	 */
	public void setFile(LocalFile file)
	{
		this.file = file;
	}
	
	/**
	 * @return the parent
	 */
	public RemoteFolder<?> getParent()
	{
		return parent;
	}
	
	/**
	 * @param parent
	 *            the parent to set
	 */
	public void setParent(RemoteFolder<?> parent)
	{
		this.parent = parent;
	}
	
	/**
	 * @return the overwrite
	 */
	public boolean isOverwrite()
	{
		return overwrite;
	}
	
	/**
	 * @param overwrite
	 *            the overwrite to set
	 */
	public void setOverwrite(boolean overwrite)
	{
		this.overwrite = overwrite;
	}
	
	/**
	 * @return the cspUploader
	 */
	public T getCspUploader()
	{
		return cspUploader;
	}
	
	/**
	 * @param cspUploader
	 *            the cspUploader to set
	 */
	public void setCspUploader(T cspUploader)
	{
		this.cspUploader = cspUploader;
	}
	
	// #endregion Getters and setters.
	// --------------------------------------------------------------------------------------
	
}
