
package com.yagasoft.overcast.container;


public abstract class File<T> extends Container<T>
{

	protected String	type;

	/**
	 * @see com.yagasoft.overcast.container.Container#isFolder()
	 */
	@Override
	public boolean isFolder()
	{
		return false;
	}

	/**
	 * @return the type
	 */
	public String getType()
	{
		return type;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(String type)
	{
		this.type = type;
	}

}
