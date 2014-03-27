/* 
 * Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
 * 
 *		Modified MIT License (GPL v3 compatible)
 * 			License terms are in a separate file (license.txt)
 * 
 *		Project/File: Overcast/com.yagasoft.overcast.container.operation/OperationEvent.java
 * 
 *			Modified: 27-Mar-2014 (16:11:19)
 *			   Using: Eclipse J-EE / JDK 7 / Windows 8.1 x64
 */

package com.yagasoft.overcast.container.operation;


import com.yagasoft.overcast.container.Container;
import com.yagasoft.overcast.container.Event;


/**
 * @author Ahmed
 * 
 */
public class OperationEvent extends Event
{
	
	/** The operation. */
	Operation		operation;
	
	/** The state. */
	OperationState	state;
	
	/** The progress. */
	protected float	progress;
	
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
