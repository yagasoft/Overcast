
package com.yagasoft.overcast.authorisation;


public abstract class UserAndPass extends Authorisation
{
	
	protected String	password;
	
	public String getPassword()
	{
		return this.password;
	}
	
	public void setPassword(String value)
	{
		this.password = value;
	}
}
