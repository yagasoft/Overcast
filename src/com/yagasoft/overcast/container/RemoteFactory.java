
package com.yagasoft.overcast.container;

import com.yagasoft.overcast.CSP;




public abstract class RemoteFactory<T, S>
{
	protected CSP csp;

	public abstract <U extends Container<T> & IRemote> U createObject(Class<U> type);
	public abstract <U extends Container<T> & IRemote> U createObject(Class<U> type, T sourceObject);
	public abstract <U extends Container<T> & IRemote> U createObjectById(Class<U> type, String id);
	public abstract <U extends Container<T> & IRemote> U createObjectByPath(Class<U> type, String path);
	
	/**
	 * @return the csp
	 */
	public CSP getCsp()
	{
		return csp;
	}
	
	/**
	 * @param csp the csp to set
	 */
	public void setCsp(CSP csp)
	{
		this.csp = csp;
	}

}




