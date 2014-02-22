
package com.yagasoft.overcast.container;


@SuppressWarnings("rawtypes")
public abstract class FileMapping<T extends RemoteFile> extends ContainerMapping
{

	private T	remote;
	private LocalFile	local;

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

	public LocalFile getLocal()
	{
		return this.local;
	}

	public void setLocal(LocalFile value)
	{
		this.local = value;
	}

	//======================================================================================
	// #endregion Getters and setters.
	////////////////////////////////////////////////////////////////////////////////////////

}
