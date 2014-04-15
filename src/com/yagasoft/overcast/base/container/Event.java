/* 
 * Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
 * 
 *		The Modified MIT Licence (GPL v3 compatible)
 * 			License terms are in a separate file (LICENCE.md)
 * 
 *		Project/File: Overcast/com.yagasoft.overcast.base.container/Event.java
 * 
 *			Modified: Apr 14, 2014 (2:26:59 PM)
 *			   Using: Eclipse J-EE / JDK 7 / Windows 8.1 x64
 */

package com.yagasoft.overcast.base.container;


/**
 * This encapsulates the vital information needed to be known in case of a container event.
 */
public abstract class Event
{
	
	/** The container. */
	protected Container<?>	container;
	
	/**
	 * Instantiates a new event.
	 * 
	 * @param container
	 *            the container
	 */
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
