
package com.yagasoft.overcast.container.transfer;

import com.yagasoft.overcast.container.Container;




public interface ITransferProgressListener
{
	public enum TransferState
	{
		INITIALISED,
		IN_PROGRESS,
		FAILED,
		COMPLETED
	}

	public void progressChanged(Container<?> container, TransferState state, float progress, Object object);
}




