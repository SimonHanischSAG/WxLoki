package wx.loki;

// -----( IS Java Code Template v1.2

import com.wm.data.*;
import com.wm.util.Values;
import com.wm.app.b2b.server.Service;
import com.wm.app.b2b.server.ServiceException;
// --- <<IS-START-IMPORTS>> ---
import com.wm.util.JournalLogger;
import com.wm.app.b2b.server.InvokeState;
import com.wm.app.b2b.server.Session;
import com.wm.lang.ns.NSName;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;
import com.softwareag.util.IDataMap;
// --- <<IS-END-IMPORTS>> ---

public final class impl

{
	// ---( internal utility methods )---

	final static impl _instance = new impl();

	static impl _newInstance() { return new impl(); }

	static impl _cast(Object o) { return (impl)o; }

	// ---( server methods )---




	public static final void continuousLokiLoggerThread (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(continuousLokiLoggerThread)>> ---
		// @sigtype java 3.5
		// [i] field:0:required url
		// [i] field:0:required user
		// [i] field:0:required pass
		// [i] field:0:optional batchSize
		// [i] field:0:optional maxDeliveryAttempts
		// [i] field:0:optional minSleepingTimeAfterBatchInMilliseconds
		// [i] field:0:optional maxSleepingTimeAfterBatchInMilliseconds
		debugLogInfo("continuousLokiLoggerThread: Thread started");
		IDataMap pipeMap = new IDataMap(pipeline);
		
		String url = pipeMap.getAsString("url");
		String user = pipeMap.getAsString("user");
		String pass = pipeMap.getAsString("pass");
		String batchSizeString = pipeMap.getAsString("batchSize");
		String maxDeliveryAttemptsString = pipeMap.getAsString("maxDeliveryAttempts");
		String minSleepingTimeAfterBatchInMillisecondsString = pipeMap.getAsString("minSleepingTimeAfterBatchInMilliseconds");
		String maxSleepingTimeAfterBatchInMillisecondsString = pipeMap.getAsString("maxSleepingTimeAfterBatchInMilliseconds");
		
		if (url == null || url.equals("")){
			debugLogInfo("continuousLokiLoggerThread aborted: url is null");
			throw new ServiceException("url is null");
		}
		
		int batchSize = 500;
		if (batchSizeString != null && !batchSizeString.equals("")) {
			batchSize = Integer.valueOf(batchSizeString);
		}
		int maxDeliveryAttempts = 1;
		if (maxDeliveryAttemptsString != null && !maxDeliveryAttemptsString.equals("")) {
			maxDeliveryAttempts = Integer.valueOf(maxDeliveryAttemptsString);
		}
		if (minSleepingTimeAfterBatchInMillisecondsString != null && !minSleepingTimeAfterBatchInMillisecondsString.equals("")) {
			minSleepingTimeAfterBatchInMilliseconds = Long.valueOf(minSleepingTimeAfterBatchInMillisecondsString);
		}
		if (maxSleepingTimeAfterBatchInMillisecondsString != null && !maxSleepingTimeAfterBatchInMillisecondsString.equals("")) {
			maxSleepingTimeAfterBatchInMilliseconds = Long.valueOf(maxSleepingTimeAfterBatchInMillisecondsString);
		}
		
		long iteration = 0;
		
		if (eventQueue != null){
			
			IData httpInput = IDataFactory.create();
			IDataMap httpInputMap = new IDataMap(httpInput);
			httpInputMap.put("url", url);
			if (user != null) {
				httpInputMap.put("user", user);
			}
			if (pass != null) {
				httpInputMap.put("pass", pass);
			}
			NSName ns = NSName.create(SERVICE_SEND_EVENT);
			
			while (stopContinuousLokiLoggerThread == false) {
				long startBatch = System.nanoTime();
				Collection<? super String> collection = new ArrayList<String>(batchSize);
				int elements = eventQueue.drainTo(collection, batchSize);
				if (elements > 0) {
					debugLogTrace("Elements: " + elements);
					
					StringBuilder jsonStringConcatenated = new StringBuilder();
					jsonStringConcatenated.append("{\"streams\":[");
					boolean firstElement = true;
					for (Object object : collection) {
						if (firstElement) {
							jsonStringConcatenated.append((String) object);
							firstElement = false;
						} else {
							jsonStringConcatenated.append(",").append((String) object);
						}
					}
					jsonStringConcatenated.append("]}");
					httpInputMap.put("jsonString", jsonStringConcatenated.toString());
					int deliveryCounter = 0;
					boolean success = false;
					do {
						deliveryCounter++;
						try {
							long start = System.nanoTime();
							Service.doInvoke(ns, httpInput);
							lastDurationOfSendEventViaHttp = System.nanoTime() - start;
							success = true;
						} catch (Exception e) {
							debugLogTrace("Error during sending to Loki: " + e.getMessage());
						}
					} while (!success && deliveryCounter < maxDeliveryAttempts);
					if (!success) {
						dropEvent(elements);
						debugLogTrace("Dropped messages");
					} else {
						sentEvents.addAndGet(elements);
						lastBatchSize = elements;
					}
					if (elements > 10 && currentSleepingTime > minSleepingTimeAfterBatchInMilliseconds) {
						currentSleepingTime -= 100;
					}
				} else {
					currentSleepingTime = maxSleepingTimeAfterBatchInMilliseconds;					
				}
				
				iteration++;
				if (iteration % 1 == 0) {
					debugLogTrace("Iteration: " + iteration);
				}
				lastDurationOfBatchRun = System.nanoTime() - startBatch;
				try {
					Thread.sleep(currentSleepingTime);
				} catch (Exception e) {
					
				}
			}
			debugLogTrace("continuousLokiLoggerThread: Loop left");
			synchronized(queueLock) {
				eventQueue = null;
				stopContinuousLokiLoggerThread = false;
				continuousLokiLoggerThreadStarted = false;
			}
		} else {
			debugLogError("continuousLokiLoggerThread: eventQueue not initialized");
			synchronized(queueLock) {
				continuousLokiLoggerThreadStarted = false;
			}
		}
		debugLogInfo("continuousLokiLoggerThread: Thread stopped now");
			
			
			
			
			
			
			
		// --- <<IS-END>> ---

                
	}



	public static final void getStateOfContinuousLokiLoggerThread (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(getStateOfContinuousLokiLoggerThread)>> ---
		// @sigtype java 3.5
		// [o] field:0:required running
		// [o] field:0:required queueSize
		// [o] field:0:required stopContinuousLokiLoggerThread
		// [o] field:0:required droppedEvents
		// [o] field:0:required currentSleepingTime
		// [o] field:0:required minSleepingTimeAfterBatchInMilliseconds
		// [o] field:0:required maxSleepingTimeAfterBatchInMilliseconds
		// [o] field:0:required tracingEnabled
		IDataMap pipeMap = new IDataMap(pipeline);
		pipeMap.put("running", String.valueOf(continuousLokiLoggerThreadStarted));
		if (eventQueue != null) {
			pipeMap.put("queueSize", String.valueOf(eventQueue.size()));
		} else {
			pipeMap.put("queueSize", "Not initialized");
		}
		pipeMap.put("stopContinuousLokiLoggerThread", String.valueOf(stopContinuousLokiLoggerThread));
		pipeMap.put("sentEvents", String.valueOf(sentEvents.get()));
		pipeMap.put("droppedEvents", String.valueOf(droppedEvents.get()));
		pipeMap.put("currentSleepingTime", String.valueOf(currentSleepingTime));
		pipeMap.put("minSleepingTimeAfterBatchInMilliseconds", String.valueOf(minSleepingTimeAfterBatchInMilliseconds));
		pipeMap.put("maxSleepingTimeAfterBatchInMilliseconds", String.valueOf(maxSleepingTimeAfterBatchInMilliseconds));
		pipeMap.put("tracingEnabled", String.valueOf(tracingEnabled));
		pipeMap.put("lastDurationOfSendEventToQueueInNanoseconds", String.valueOf(lastDurationOfSendEventToQueue));
		pipeMap.put("lastDurationOfSendEventViaHttpInMilliseconds", String.valueOf(lastDurationOfSendEventViaHttp/1000000));
		pipeMap.put("lastDurationOfBatchRunInMilliseconds", String.valueOf(lastDurationOfBatchRun/1000000));
		pipeMap.put("lastBatchSize", String.valueOf(lastBatchSize));
		
			
			
			
			
		// --- <<IS-END>> ---

                
	}



	public static final void sendEventToQueue (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(sendEventToQueue)>> ---
		// @sigtype java 3.5
		// [i] field:0:required messageAsJsonString
		long start = System.nanoTime();
		debugLogTrace("sendEventToQueue invoked");
		IDataMap pipeMap = new IDataMap(pipeline);
		String messageAsJsonString = pipeMap.getAsString("messageAsJsonString");
		if (eventQueue != null) {
			
			boolean inserted = eventQueue.offer(messageAsJsonString);
			if (!inserted) {
				dropEvent(1);
				debugLogTrace("sendEventToQueue: Message dropped as queue is full");
			}
		} else {
			dropEvent(1);
			debugLogTrace("sendEventToQueue: not initialized. Message: " + messageAsJsonString);
		}
			
		lastDurationOfSendEventToQueue = System.nanoTime() - start;
		
			
		// --- <<IS-END>> ---

                
	}



	public static final void startDebugLogging (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(startDebugLogging)>> ---
		// @sigtype java 3.5
		//Trace.enableTracing();
		tracingEnabled = true;
			
			
			
		// --- <<IS-END>> ---

                
	}



	public static final void startLokiLoggerThread (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(startLokiLoggerThread)>> ---
		// @sigtype java 3.5
		// [i] field:0:required url
		// [i] field:0:required token
		// [i] field:0:optional batchSize
		// [i] field:0:optional maxMessagesInQueue
		// [i] field:0:optional maxDeliveryAttempts
		// [i] field:0:optional minSleepingTimeAfterBatchInMilliseconds
		// [i] field:0:optional maxSleepingTimeAfterBatchInMilliseconds
		// [i] field:0:optional enableTracing
		try {
			// Clone to avoid problems with later dropps:
			IData pipelineClone = IDataUtil.deepClone(pipeline);
					IDataMap pipeMap = new IDataMap(pipelineClone);
		
		
			if (continuousLokiLoggerThreadStarted) {
				stopContinuousLokiLoggerThreadStartedImpl();
			}
			
			
			synchronized(queueLock) {
			
				String enableTracing = pipeMap.getAsString("enableTracing");
				if ("true".equals(enableTracing)) {
					tracingEnabled = true;
				} else {
					tracingEnabled = false;
				}
				
				continuousLokiLoggerThreadStarted = true;
				
				if (eventQueue != null) {
					debugLogError("startLoggerThread: Drop old eventQueue");
				}
				String maxMessagesInQueueString = pipeMap.getAsString("maxMessagesInQueue");
				int maxMessagesInQueue = 100000;
				if (maxMessagesInQueueString != null && !maxMessagesInQueueString.equals("")) {
					maxMessagesInQueue = Integer.valueOf(maxMessagesInQueueString);
				}
			
				eventQueue = new ArrayBlockingQueue<String>(maxMessagesInQueue, true);
				
				droppedEvents = new AtomicLong();
				sentEvents = new AtomicLong();
				
				//IData input = IDataFactory.create();
				NSName ns = NSName.create(SERVICE_LOGGER_THREAD);
				Session session = InvokeState.getCurrentSession();
				Service.doThreadInvoke(ns, session, pipelineClone);
				
				debugLogInfo("startLokiLoggerThread: Started");
			}
		} catch (Exception e) {
			throw new ServiceException(e);
		}
			
			
			
			
		// --- <<IS-END>> ---

                
	}



	public static final void stopDebugLogging (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(stopDebugLogging)>> ---
		// @sigtype java 3.5
		tracingEnabled = false;
			
			
			
		// --- <<IS-END>> ---

                
	}



	public static final void stopLokiLoggerThread (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(stopLokiLoggerThread)>> ---
		// @sigtype java 3.5
		debugLogTrace("stopLokiLoggerThread: Stop thread now");
		stopContinuousLokiLoggerThreadStartedImpl();
			
			
		// --- <<IS-END>> ---

                
	}

	// --- <<IS-START-SHARED>> ---
	
	private static Object queueLock = new Object();
	private static ArrayBlockingQueue<String> eventQueue = null;
	private static boolean continuousLokiLoggerThreadStarted = false;
	private static boolean stopContinuousLokiLoggerThread = false;
	private static long minSleepingTimeAfterBatchInMilliseconds = 100;
	private static long maxSleepingTimeAfterBatchInMilliseconds = 1000;
	private static AtomicLong droppedEvents = new AtomicLong();
	private static AtomicLong sentEvents = new AtomicLong();
	private static boolean tracingEnabled = false;
	private static long currentSleepingTime = maxSleepingTimeAfterBatchInMilliseconds;
	private static String SERVICE_SEND_EVENT = "wx.loki.impl:sendJsonStringToLoki";
	private static String SERVICE_LOGGER_THREAD = "wx.loki.impl:continuousLokiLoggerThread";
	private static String LOG_FUNCTION = "WxLoki";
	private static long lastDurationOfSendEventToQueue = -1;
	private static long lastDurationOfSendEventViaHttp = -1;
	private static long lastDurationOfBatchRun = -1;
	private static int lastBatchSize = -1;
	
	private static void dropEvent(long count) {
		droppedEvents.addAndGet(count);
	}
	
	private static void debugLogTrace(String message) {
		if (tracingEnabled) {
			// Log as INFO, otherwise it would be lost:
			JournalLogger.log(4,  JournalLogger.FAC_FLOW_SVC, JournalLogger.INFO, LOG_FUNCTION, message);
		}
	}
	
	private static void debugLogError(String message) {
		JournalLogger.log(4,  JournalLogger.FAC_FLOW_SVC, JournalLogger.ERROR, LOG_FUNCTION, message);
	}
	
	private static void debugLogInfo(String message) {
		JournalLogger.log(4,  JournalLogger.FAC_FLOW_SVC, JournalLogger.INFO, LOG_FUNCTION, message);
	}
	
	private static void stopContinuousLokiLoggerThreadStartedImpl() {
		if (continuousLokiLoggerThreadStarted) {
			synchronized(queueLock) {
				stopContinuousLokiLoggerThread = true;
			}
			int counter = 0;
			do {
				counter++;
				try {
					debugLogTrace("continuousLokiLoggerThreadStarted: Wait for stopping");
					Thread.sleep(currentSleepingTime);
				} catch (Exception e) {
					
				}
			}
			while(continuousLokiLoggerThreadStarted && counter < 20);
			if (continuousLokiLoggerThreadStarted) {
				debugLogError("continuousLokiLoggerThreadStarted: Could not stop");
				synchronized(queueLock) {
					// re-init for restarting thread:
					stopContinuousLokiLoggerThread = false;
					continuousLokiLoggerThreadStarted = false;
				}
			}
		}
	}
		
		
		
		
		
		
		
		
		
		
	// --- <<IS-END-SHARED>> ---
}

