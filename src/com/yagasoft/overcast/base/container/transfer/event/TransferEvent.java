/*
 * Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
 *
 *		The Modified MIT Licence (GPL v3 compatible)
 * 			Licence terms are in a separate file (LICENCE.md)
 *
 *		Project/File: Overcast/com.yagasoft.overcast.base.container.transfer.event/TransferEvent.java
 *
 *			Modified: 14-Jun-2014 (01:13:36)
 *			   Using: Eclipse J-EE / JDK 8 / Windows 8.1 x64
 */

package com.yagasoft.overcast.base.container.transfer.event;


import com.yagasoft.overcast.base.container.Container;
import com.yagasoft.overcast.base.container.Event;
import com.yagasoft.overcast.base.container.transfer.TransferJob;


/**
 * A class representing a collection of information about a transfer event.
 */
public class TransferEvent extends Event
{
	
	/** The state. */
	protected TransferState		state;
	
	/** The progress. */
	protected float				progress;
	
	/** Transfer job. */
	protected TransferJob<?>	job;
	
	/** Destination container -- NOT destination parent. */
	protected Container<?>		destination;
	
	/**
	 * Instantiates a new transfer event.
	 *
	 * @param job
	 *            the job.
	 * @param state
	 *            the state.
	 * @param progress
	 *            the progress.
	 */
	public TransferEvent(TransferJob<?> job, TransferState state, float progress)
	{
		super(job.getSourceFile());
		this.job = job;
		this.state = state;
		this.progress = progress;
		destination = job.getDestinationFile();
	}
	
	// --------------------------------------------------------------------------------------
	// #region Getters and setters.
	
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
	 * @return the job
	 */
	public TransferJob<?> getJob()
	{
		return job;
	}
	
	/**
	 * @param job
	 *            the job to set
	 */
	public void setJob(TransferJob<?> job)
	{
		this.job = job;
	}
	
	/**
	 * @return the destination
	 */
	public Container<?> getDestination()
	{
		return destination;
	}
	
	/**
	 * @param destination
	 *            the destination to set
	 */
	public void setDestination(Container<?> destination)
	{
		this.destination = destination;
	}
	
	// #endregion Getters and setters.
	// --------------------------------------------------------------------------------------
	
}
