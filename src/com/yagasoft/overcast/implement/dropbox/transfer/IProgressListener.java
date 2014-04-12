
package com.yagasoft.overcast.implement.dropbox.transfer;

import com.yagasoft.overcast.base.container.transfer.TransferState;




public interface IProgressListener
{
	public void progressChanged(UploadJob uploadJob, TransferState state, float progress);

	public void progressChanged(DownloadJob downloadJob, TransferState state, float progress);
}
