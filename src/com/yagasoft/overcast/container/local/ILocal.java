package com.yagasoft.overcast.container.local;

import com.yagasoft.overcast.container.remote.RemoteFolder;
import com.yagasoft.overcast.container.transfer.ITransferProgressListener;
import com.yagasoft.overcast.exception.TransferException;




public interface ILocal
{	
	public void upload(RemoteFolder<?> parent, boolean overwrite, ITransferProgressListener listener, Object object)
			throws TransferException;

}
