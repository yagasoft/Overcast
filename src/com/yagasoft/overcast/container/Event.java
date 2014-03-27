/* 
 * Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
 * 
 *		Modified MIT License (GPL v3 compatible)
 * 			License terms are in a separate file (license.txt)
 * 
 *		Project/File: Overcast/com.yagasoft.overcast.container/Event.java
 * 
 *			Modified: 27-Mar-2014 (16:12:08)
 *			   Using: Eclipse J-EE / JDK 7 / Windows 8.1 x64
 */

package com.yagasoft.overcast.container;


/**
 * @author Ahmed
 * 
 */
public abstract class Event
{
	
	/** The container. */
	protected Container<?>	container;
	
	public Event(Container<?> container)
	{
		this.container = container;
	}
	
	/**
	 * Gets the container.
	 * 
	 * @return the container
	 */
	public Container<?> getContainer()
	{
		return container;
	}
	
	/**
	 * Sets the container.
	 * 
	 * @param container
	 *            the new container
	 */
	public void setContainer(Container<?> container)
	{
		this.container = container;
	}
	
}
