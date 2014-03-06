
package com.yagasoft.overcast.container.transfer;


import com.yagasoft.overcast.container.remote.RemoteFile;
import com.yagasoft.overcast.container.local.LocalFolder;


public final class DownloadJob<T>
{

	RemoteFile<?>		file;
	LocalFolder	parent;
	boolean			overwrite;
	T				cspDownloader;

	public DownloadJob(RemoteFile<?> file, LocalFolder parent, boolean overwrite, T cspUploader)
	{
		this.file = file;
		this.parent = parent;
		this.overwrite = overwrite;
		this.cspDownloader = cspUploader;
	}

	// --------------------------------------------------------------------------------------
	// #region Getters and setters.

	/**
	 * @return the file
	 */
	public RemoteFile<?> getFile()
	{
		return file;
	}

	/**
	 * @param file
	 *            the file to set
	 */
	public void setFile(RemoteFile<?> file)
	{
		this.file = file;
	}

	/**
	 * @return the parent
	 */
	public LocalFolder getParent()
	{
		return parent;
	}

	/**
	 * @param parent
	 *            the parent to set
	 */
	public void setParent(LocalFolder parent)
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
	public T getCspDownloader()
	{
		return cspDownloader;
	}

	/**
	 * @param cspUploader
	 *            the cspUploader to set
	 */
	public void setCspDownloader(T cspDownloader)
	{
		this.cspDownloader = cspDownloader;
	}

	// #endregion Getters and setters.
	// --------------------------------------------------------------------------------------

}
