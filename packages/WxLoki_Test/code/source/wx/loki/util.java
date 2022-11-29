package wx.loki;

// -----( IS Java Code Template v1.2

import com.wm.data.*;
import com.wm.util.Values;
import com.wm.app.b2b.server.Service;
import com.wm.app.b2b.server.ServiceException;
// --- <<IS-START-IMPORTS>> ---
// --- <<IS-END-IMPORTS>> ---

public final class util

{
	// ---( internal utility methods )---

	final static util _instance = new util();

	static util _newInstance() { return new util(); }

	static util _cast(Object o) { return (util)o; }

	// ---( server methods )---




	public static final void sleep (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(sleep)>> ---
		// @sigtype java 3.5
		// [i] field:0:required sleepTimeInMilliSeconds
		IDataCursor pipelineCursor = pipeline.getCursor();
		Integer sleepTimeInMilliSeconds = IDataUtil.getInt(pipelineCursor,
				"sleepTimeInMilliSeconds", -1);
		pipelineCursor.destroy();
		
		if (sleepTimeInMilliSeconds != -1){
			try {
				Thread.sleep(sleepTimeInMilliSeconds);
			} catch (InterruptedException e) {
				throw new ServiceException(e);
			}
		}
		// --- <<IS-END>> ---

                
	}
}

