
package com.yagasoft.overcast.container.remote;


import com.yagasoft.overcast.container.local.LocalFolder;
import com.yagasoft.overcast.container.transfer.ITransferProgressListener;
import com.yagasoft.overcast.exception.TransferException;


public interface IRemote
{
	
	public void download(LocalFolder parent, boolean overwrite, ITransferProgressListener listener, Object object)
			throws TransferException;
}
