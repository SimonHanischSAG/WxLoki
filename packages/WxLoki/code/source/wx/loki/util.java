package wx.loki;

// -----( IS Java Code Template v1.2

import com.wm.data.*;
import com.wm.util.Values;
import com.wm.app.b2b.server.Service;
import com.wm.app.b2b.server.ServiceException;
// --- <<IS-START-IMPORTS>> ---
import com.softwareag.util.IDataMap;
import java.net.InetAddress;
import java.util.Locale;
// --- <<IS-END-IMPORTS>> ---

public final class util

{
	// ---( internal utility methods )---

	final static util _instance = new util();

	static util _newInstance() { return new util(); }

	static util _cast(Object o) { return (util)o; }

	// ---( server methods )---




	public static final void getHostname (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(getHostname)>> ---
		// @sigtype java 3.5
		// [o] field:0:required hostName
		String host = null;
		
		// pipeline
		
		try {
			host = InetAddress.getLocalHost().getCanonicalHostName().toLowerCase();
		} catch (Exception e) {} 
		
		
		
		
		
		IDataCursor pipelineCursor = pipeline.getCursor();
		IDataUtil.put( pipelineCursor, "hostName", host );
		pipelineCursor.destroy();
		
		
			
			
			
			
		// --- <<IS-END>> ---

                
	}



	public static final void getLokiTime (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(getLokiTime)>> ---
		// @sigtype java 3.5
		// [o] field:0:required time
		IDataMap pipelineMap = new IDataMap(pipeline);
		pipelineMap.put("time", String.format(Locale.US, "%.3f", Double.valueOf(System.currentTimeMillis()/ 1000.0)));
			
		// --- <<IS-END>> ---

                
	}



	public static final void throwError (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(throwError)>> ---
		// @sigtype java 3.5
		// [i] field:0:required msg
		// pipeline
		IDataCursor pipelineCursor = pipeline.getCursor();
		
		String	msg = IDataUtil.getString( pipelineCursor, "msg" );
		pipelineCursor.destroy();
		
		throw new ServiceException(msg);			
			
			
		// --- <<IS-END>> ---

                
	}
}

