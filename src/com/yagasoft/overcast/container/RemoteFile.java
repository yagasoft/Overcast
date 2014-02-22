
package com.yagasoft.overcast.container;

import java.net.URL;


public abstract class RemoteFile<T> extends File<T>
{
	protected URL link;
	
	public RemoteFile()
	{}
	
	public RemoteFile(T file)
	{
		sourceObject = file;
		updateInfo();
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
