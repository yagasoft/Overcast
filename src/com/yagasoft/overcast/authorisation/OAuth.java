
package com.yagasoft.overcast.authorisation;

import java.nio.file.Path;


public abstract class OAuth extends Authorisation
{
	
	protected Path	info;
	protected int				token;
	protected int				refreshToken;
	
	public abstract void acquirePermission();
	
	public abstract void reacquirePermission();
	
	public abstract void saveToken();
	
	// //////////////////////////////////////////////////////////////////////////////////////
	// #region Getters and setters.
	// ======================================================================================
	
	public java.nio.file.Path getInfo()
	{
		return this.info;
	}
	
	public void setInfo(Path value)
	{
		this.info = value;
	}
	
	public int getToken()
	{
		return this.token;
	}
	
	public void setToken(int value)
	{
		this.token = value;
	}
	
	public int getRefreshToken()
	{
		return this.refreshToken;
	}
	
	public void setRefreshToken(int value)
	{
		this.refreshToken = value;
	}
	
	// ======================================================================================
	// #endregion Getters and setters.
	// //////////////////////////////////////////////////////////////////////////////////////
	
}
