
package com.yagasoft.overcast.container;


import java.util.HashMap;

import com.yagasoft.overcast.CSP;
import com.yagasoft.overcast.container.transfer.ITransferProgressListener;
import com.yagasoft.overcast.container.transfer.ITransferrable;
import com.yagasoft.overcast.container.transfer.ITransferProgressListener.TransferState;
import com.yagasoft.overcast.exception.AccessException;


public abstract class Container<T> implements ITransferrable
{

	protected String										id;
	protected String										name;
	protected String										path;
	protected long											size;
	protected T												sourceObject;
	protected Folder<?>										parent;
	protected HashMap<ITransferProgressListener, Object>	progressListeners	= new HashMap<ITransferProgressListener, Object>();
	protected CSP											csp;

	public void generateId()
	{
		id = path;
	}

	/**
	 * Checks if the file exists physically or not.
	 *
	 * @return true, if it exists
	 * @throws AccessException
	 */
	public abstract boolean isExist() throws AccessException;

	public abstract boolean isFolder();

	/**
	 * Update the fields (class attributes) in this file object from the
	 * in-memory info (nothing is done outside the program).
	 */
	public abstract void updateInfo();

	/**
	 * Update from where the file resides. It reads the meta of the file.
	 */
	public abstract void updateFromSource();

	public abstract Container<?> copy(Folder<?> destination, boolean overwrite);

	public abstract void move(Folder<?> destination, boolean overwrite);

	public abstract void rename(String newName);

	public abstract void delete();

	/**
	 * @see com.yagasoft.overcast.container.transfer.ITransferrable#addProgressListener(com.yagasoft.overcast.container.transfer.ITransferProgressListener,
	 *      java.lang.Object)
	 */
	@Override
	public void addProgressListener(ITransferProgressListener listener, Object object)
	{
		progressListeners.put(listener, object);
	}

	/**
	 * @see com.yagasoft.overcast.container.transfer.ITransferrable#removeProgressListener(com.yagasoft.overcast.container.transfer.ITransferProgressListener)
	 */
	@Override
	public void removeProgressListener(ITransferProgressListener listener)
	{
		progressListeners.remove(listener);
	}

	/**
	 * @see com.yagasoft.overcast.container.transfer.ITransferrable#notifyListeners(com.yagasoft.overcast.container.transfer.ITransferProgressListener.TransferState,
	 *      float)
	 */
	@Override
	public void notifyListeners(TransferState state, float progress)
	{
		for (ITransferProgressListener listener : progressListeners.keySet())
		{
			listener.progressChanged(this, state, progress, progressListeners.get(listener));
		}

		if (state == TransferState.COMPLETED)
		{
			clearListeners();
		}
	}

	/**
	 * @see com.yagasoft.overcast.container.transfer.ITransferrable#clearListeners()
	 */
	@Override
	public void clearListeners()
	{
		progressListeners.clear();
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object object)
	{
		return ((object instanceof Container) && (((Container<?>) object).id.equalsIgnoreCase(getId())));
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	// #region Getters and setters.
	// ======================================================================================

	/**
	 * @return the id
	 */
	public String getId()
	{
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(String id)
	{
		this.id = id;
	}

	public String getName()
	{
		return this.name;
	}

	public void setName(String value)
	{
		this.name = value;
	}

	public String getPath()
	{
		return this.path;
	}

	public void setPath(String value)
	{
		this.path = value;
	}

	/**
	 * @return the size
	 */
	public long getSize()
	{
		return size;
	}

	/**
	 * Sets the size.
	 *
	 * @param size
	 *            the size to set
	 */
	public void setSize(long size)
	{
		this.size = size;
	}

	/**
	 * @return the sourceObject
	 */
	public T getSourceObject()
	{
		return sourceObject;
	}

	/**
	 * @param sourceObject
	 *            the sourceObject to set
	 */
	public void setSourceObject(T sourceObject)
	{
		this.sourceObject = sourceObject;
	}

	/**
	 * @return the parent
	 */
	public Folder<?> getParent()
	{
		return parent;
	}

	/**
	 * @param parent
	 *            the parent to set
	 */
	public void setParent(Folder<?> parent)
	{
		this.parent = parent;
	}

	/**
	 * @return the csp
	 */
	public CSP getCsp()
	{
		return csp;
	}

	/**
	 * @param csp
	 *            the csp to set
	 */
	public void setCsp(CSP csp)
	{
		this.csp = csp;
	}

	// ======================================================================================
	// #endregion Getters and setters.
	// //////////////////////////////////////////////////////////////////////////////////////

}
