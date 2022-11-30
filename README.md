# WxLoki
This package encapsulate the process of sending your log messages to Loki in an asynchronous and performant way directly from Flow.

For better performance it buffers the log messages in a queue in memory. A thread in background works through the queue and sending the events in batches to Loki (DO NOT KILL THIS THREAD!).
Between the batches this "continuousLokiLoggerThread" will sleep a period of time. This time is depending on the load and the min and max which is configurable.

As the data is stored in a queue in memory this technique has the disadvantage **that message lost cannot be completely excluded**. On the other hand the direct invocation from Flow allows you to attach your **own labels** which can be used for building queries and dashboards.
The official alternative to this package is to use **Promtail as an agent on each server** (https://grafana.com/docs/loki/latest/clients/promtail/). 
If you use that you have to define regular expressions if you want to extract labels from log lines.

It is designed for usage together with the official packages WxConfig (or the free alternative https://github.com/SimonHanischSAG/WxConfigLight) and optionally in parallel with the official packages WxLog or WxLog2.

<h2>How to use</h2>

<h3>Deploy/checkout WxLoki</h3>

Check under releases for a proper release and deploy it. Otherwise you can check out the latest version from GIT and create a link like this:

mklink /J F:\\SoftwareAG\\IntegrationServer\\instances\\default\\packages\\WxLoki F:\\GIT-Repos\\WxLoki\\packages\\WxLoki

For demonstration purpose you can also link the Test-package which simulates the Loki endpoint:
mklink /J F:\\SoftwareAG\\IntegrationServer\\instances\\default\\packages\\WxLoki_Test F:\\GIT-Repos\\WxLoki\\packages\\WxLoki_Test

<h4>Build & Reload</h4>

If you checkout the sources from GitHub you have to compile the source, e.g. with:

C:\SoftwareAG\IntegrationServer\instances\default\bin\jcode.bat makeall WxLoki

Reload WxLoki

Do the same for WxLoki_Test if you want to use the test stuff.

<h3>Configure Environment</h3>

You have to configure WxLoki in ../../../config/packages/WxLoki/wxconfig-&lt;env&gt;.cnf (example when you are using the simulation in WxLoki_Test. 
In that case you have to adjust the permissions of wx.loki.ws:_post e.g. to Anonymous):

<pre><code>
loki.logging.url=http://localhost:5555/rest/wx.loki.ws
loki.logging.user=Administrator
loki.logging.pass=manage
loki.logging.enabled=true
</code></pre>

Reload WxLoki. The startup will start the "continuousLokiLoggerThread" with that configuration. Check the server.log for:
  
2022-11-29 09:38:00 CEST [ISS.0028.0012I] (tid=86) WxLoki: Startup service (wx.loki.admin:startUp) 
2022-11-29 09:38:01 CEST [ISP.0090.0004I] (tid=86) WxLoki -- startLokiLoggerThread: Started 
2022-11-29 09:38:01 CEST [ISP.0090.0004I] (tid=86) WxLoki -- continuousLokiLoggerThread: Thread started 

CONSIDER THAT ALL CONFIG VALUES ARE CACHED except loki.logging.enabled which is checked for each log statement.

<h3>Test Configuration</h3>

You can invoke wx.loki.test:sendEventDirectlyToLoki in order to test your configuration directly. Consider that Loki is returning "204: No Content" per case of success!
  
Furthermore you can invoke wx.loki.test:testContinuousLokiLogger in order to test the continuousLokiLogger


<h3>Advanded Configuration</h3>

Compare with ../../../config/packages/WxLoki/wxconfig-&lt;env&gt;.cnf.cnf to optimize the behavior of the internal queue and thread:

<pre><code>
# Count of retries for sending messages to Loki -> Higher retries in case of outage of Loki will prevent from messsage lost
loki.logging.maxDeliveryAttempts=100
# Count of messages which can be stored in the internal queue and waiting for sending -> Avoid to store too many messages in memory in order to break your server.
loki.logging.maxMessagesInQueue=100000

# How many message shall be written to Loki in one call -> Higher values means better performance as the overhead of HTTP is huge
loki.logging.batchSize=2000
# How often should wx.loki.impl:continuousLokiLoggerThread become active: -> After each call the will sleep a dynamic period of time between the following values. You can reduce CPU usage of this thread by increasing minSleepingTimeAfterBatchInMilliseconds
loki.logging.minSleepingTimeAfterBatchInMilliseconds=100
loki.logging.maxSleepingTimeAfterBatchInMilliseconds=500
</code></pre>

If you want to check how the thread is performing you can invoke
wx.loki.impl:getStateOfContinuousLokiLoggerThread
during runtime.

<h3>Best practices</h3>

You should work carefully with the labels. Unfortunately they are not designed for a highly dynamic usage (both for the key and the value). Comapre with: https://grafana.com/docs/loki/latest/best-practices/
