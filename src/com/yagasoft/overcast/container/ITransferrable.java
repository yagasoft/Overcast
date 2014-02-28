
package com.yagasoft.overcast.container;

import com.yagasoft.overcast.container.ITransferProgressListener.TransferState;




public interface ITransferrable
{	
	public void addProgressListener(ITransferProgressListener listener, Object object);
	public void removeProgressListener(ITransferProgressListener listener, Object object);
	public void notifyListeners(TransferState state, float progress);
	public void clearListeners();
}




