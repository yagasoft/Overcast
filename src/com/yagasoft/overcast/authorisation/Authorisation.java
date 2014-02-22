
package com.yagasoft.overcast.authorisation;


public abstract class Authorisation
{
	
	protected String	userID;
	
	public abstract void authorise();
	
	public String getUserID()
	{
		return this.userID;
	}
	
	public void setUserID(String value)
	{
		this.userID = value;
	}
}
