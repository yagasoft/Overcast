
package com.yagasoft.overcast.authorisation;


public abstract class UserAndPass extends Authorisation
{
	
	protected String	username;
	protected String	password;
	
	
	/**
	 * @return the username
	 */
	public String getUsername()
	{
		return username;
	}

	
	/**
	 * @param username the username to set
	 */
	public void setUsername(String username)
	{
		this.username = username;
	}

	public String getPassword()
	{
		return this.password;
	}
	
	public void setPassword(String value)
	{
		this.password = value;
	}
}
