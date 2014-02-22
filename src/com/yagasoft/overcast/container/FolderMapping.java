
package com.yagasoft.overcast.container;


@SuppressWarnings("rawtypes")
public abstract class FolderMapping<T extends RemoteFolder> extends ContainerMapping
{
	
	private T	remote;
	private LocalFolder		local;
	
	////////////////////////////////////////////////////////////////////////////////////////
	// #region Getters and setters.
	//======================================================================================
	
	public T getRemote()
	{
		return this.remote;
	}
	
	public void setRemote(T value)
	{
		this.remote = value;
	}
	
	public LocalFolder getLocal()
	{
		return this.local;
	}
	
	public void setLocal(LocalFolder value)
	{
		this.local = value;
	}
	
	//======================================================================================
	// #endregion Getters and setters.
	////////////////////////////////////////////////////////////////////////////////////////

}
