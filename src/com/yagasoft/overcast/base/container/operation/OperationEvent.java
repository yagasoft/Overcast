/* 
 * Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
 * 
 *		The Modified MIT Licence (GPL v3 compatible)
 * 			License terms are in a separate file (LICENCE.md)
 * 
 *		Project/File: Overcast/com.yagasoft.overcast.base.container.operation/OperationEvent.java
 * 
 *			Modified: Apr 15, 2014 (9:05:03 AM)
 *			   Using: Eclipse J-EE / JDK 7 / Windows 8.1 x64
 */

package com.yagasoft.overcast.base.container.operation;


import com.yagasoft.overcast.base.container.Container;
import com.yagasoft.overcast.base.container.Event;


/**
 * An event class containing all the vital information of an operation event.
 */
public class OperationEvent extends Event
{
	
	/** The operation. */
	Operation		operation;
	
	/** The state. */
	OperationState	state;
	
	/** The progress. */
	protected float	progress;
	
	/**
	 * Instantiates a new operation event.
	 * 
	 * @param container
	 *            the container relating to the event.
	 * @param operation
	 *            the operation enum
	 * @param state
	 *            the current state of the operation
	 * @param progress
	 *            the progress
	 */
	public OperationEvent(Container<?> container, Operation operation, OperationState state, float progress)
	{
		super(container);
		this.state = state;
		this.progress = progress;
		this.operation = operation;
	}
	
	/**
	 * Gets the state.
	 * 
	 * @return the state
	 */
	public OperationState getState()
	{
		return state;
	}
	
	/**
	 * Sets the state.
	 * 
	 * @param state
	 *            the new state
	 */
	public void setState(OperationState state)
	{
		this.state = state;
	}
	
	/**
	 * Gets the progress.
	 * 
	 * @return the progress
	 */
	public float getProgress()
	{
		return progress;
	}
	
	/**
	 * Sets the progress.
	 * 
	 * @param progress
	 *            the new progress
	 */
	public void setProgress(float progress)
	{
		this.progress = progress;
	}
	
	/**
	 * Gets the operation.
	 * 
	 * @return the operation
	 */
	public Operation getOperation()
	{
		return operation;
	}
	
	/**
	 * Sets the operation.
	 * 
	 * @param operation
	 *            the new operation
	 */
	public void setOperation(Operation operation)
	{
		this.operation = operation;
	}
	
}
