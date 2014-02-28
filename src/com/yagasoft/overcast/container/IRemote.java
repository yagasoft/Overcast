
package com.yagasoft.overcast.container;




public interface IRemote
{	
	public void download(Folder<?> parent, boolean overwrite, ITransferProgressListener listener, Object object) throws Exception;
}




