/* 
 * Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
 * 
 *		The Modified MIT Licence (GPL v3 compatible)
 * 			License terms are in a separate file (LICENCE.md)
 * 
 *		Project/File: Overcast/com.yagasoft.overcast.base.container.content/ChangeEvent.java
 * 
 *			Modified: May 3, 2014 (8:21:16 AM)
 *			   Using: Eclipse J-EE / JDK 7 / Windows 8.1 x64
 */

package com.yagasoft.overcast.base.container.content;


import com.yagasoft.overcast.base.container.Container;
import com.yagasoft.overcast.base.container.Event;


/**
 * An event class containing all the vital information of a change event.
 */
public class ChangeEvent extends Event
{
	
	/** The operation. */
	Change			change;
	
	/** The object of the change. */
	Container<?>	object;
	
	/**
	 * Instantiates a new operation event.
	 * 
	 * @param container
	 *            the container relating to the event.
	 * @param change
	 *            the change enum
	 * @param object
	 *            the object of the change
	 */
	public ChangeEvent(Container<?> container, Change change, Container<?> object)
	{
		super(container);
		this.change = change;
		this.object = object;
	}
	
	/**
	 * Gets the change.
	 * 
	 * @return the change
	 */
	public Change getChange()
	{
		return change;
	}
	
	/**
	 * Sets the change.
	 * 
	 * @param change
	 *            the new change
	 */
	public void setChange(Change change)
	{
		this.change = change;
	}
	
	/**
	 * Gets the object.
	 * 
	 * @return the object
	 */
	public Container<?> getObject()
	{
		return object;
	}
	
	/**
	 * Sets the object.
	 * 
	 * @param object
	 *            the new object
	 */
	public void setObject(Container<?> object)
	{
		this.object = object;
	}
	
}
