/*
 * Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
 *
 *		Modified MIT License (GPL v3 compatible)
 * 			License terms are in a separate file (license.txt)
 *
 */
package com.yagasoft.overcast.container.transfer;

import com.yagasoft.overcast.container.Container;


/**
 * A class representing a collection of information about a transfer event.
 */
public class TransferEvent
{

	/** The container. */
	Container<?> container;
	
	/** The state. */
	TransferState state;
	
	/** The progress. */
	float progress;
	
	/** The object. */
	Object object;


	/**
	 * Instantiates a new transfer event.
	 *
	 * @param container
	 *            the container
	 * @param state
	 *            the state
	 * @param progress
	 *            the progress
	 * @param object
	 *            the object
	 */
	public TransferEvent(Container<?> container, TransferState state, float progress, Object object)
	{
		this.container = container;
		this.state = state;
		this.progress = progress;
		this.object = object;
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


	
	/**
	 * Gets the state.
	 * 
	 * @return the state
	 */
	public TransferState getState()
	{
		return state;
	}


	
	/**
	 * Sets the state.
	 * 
	 * @param state
	 *            the new state
	 */
	public void setState(TransferState state)
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
	 * Gets the object.
	 * 
	 * @return the object
	 */
	public Object getObject()
	{
		return object;
	}


	
	/**
	 * Sets the object.
	 * 
	 * @param object
	 *            the new object
	 */
	public void setObject(Object object)
	{
		this.object = object;
	}


}
