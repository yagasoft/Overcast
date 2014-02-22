
package com.yagasoft.overcast.container;

import java.net.URL;


public abstract class RemoteFolder<T> extends Folder<T>
{
	protected URL link;
	
	public RemoteFolder()
	{}
	
	public RemoteFolder(T file)
	{
		sourceObject = file;
		updateInfo(false, false);
	}

	
	/**
	 * @return the link
	 */
	public URL getLink()
	{
		return link;
	}

	
	/**
	 * @param link the link to set
	 */
	public void setLink(URL link)
	{
		this.link = link;
	}
	
}
