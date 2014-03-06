
package com.yagasoft.overcast.container.transfer;

import com.yagasoft.overcast.container.transfer.ITransferProgressListener.TransferState;




public interface ITransferrable
{
	public void addProgressListener(ITransferProgressListener listener, Object object);
	public void removeProgressListener(ITransferProgressListener listener);
	public void notifyListeners(TransferState state, float progress);
	public void clearListeners();
}




