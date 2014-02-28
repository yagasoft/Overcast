
package com.yagasoft.overcast.container;


import java.net.URL;


public abstract class RemoteFolder<T> extends Folder<T> implements IRemote
{
	
	protected URL		link;
	protected LocalFolder	localMapping;
	
	// --------------------------------------------------------------------------------------
	// #region Getters and setters.
	
	/**
	 * @return the link
	 */
	public URL getLink()
	{
		return link;
	}
	
	/**
	 * @param link
	 *            the link to set
	 */
	public void setLink(URL link)
	{
		this.link = link;
	}
	
	/**
	 * @return the localMapping
	 */
	public LocalFolder getLocalMapping()
	{
		return localMapping;
	}
	
	/**
	 * @param localMapping
	 *            the localMapping to set
	 */
	public void setLocalMapping(LocalFolder localMapping)
	{
		this.localMapping = localMapping;
	}
	
	// #endregion Getters and setters.
	// --------------------------------------------------------------------------------------
	
}
