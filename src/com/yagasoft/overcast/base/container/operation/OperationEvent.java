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
	protected Operation			operation;

	/** The state. */
	protected OperationState	state;

	/** The progress. */
	protected float				progress;

	/** The object of the change. */
	protected Container<?>				object;

	public OperationEvent(Container<?> container)
	{
		this(container, Operation.UPDATE, null);
	}

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
	public OperationEvent(Container<?> container, Operation operation, Container<?> object)
	{
		this(container, operation, null, 1.0f);
		this.object = object;
	}

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

	public OperationEvent(Container<?> container, Operation operation, OperationState state, float progress, Container<?> object)
	{
		super(container);
		this.state = state;
		this.progress = progress;
		this.operation = operation;
		this.object = object;
	}

	////////////////////////////////////////////////////////////////////////////////////////
	// #region Getters and setters.
	//======================================================================================

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


	/**
	 * @return the object
	 */
	public Container<?> getObject()
	{
		return object;
	}


	/**
	 * @param object the object to set
	 */
	public void setObject(Container<?> object)
	{
		this.object = object;
	}

	//======================================================================================
	// #endregion Getters and setters.
	////////////////////////////////////////////////////////////////////////////////////////

}
