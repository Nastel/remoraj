# Remora Control 

Remora control plugin enables you to control remoraJ  java agent on demand over HTTP/REST.
By default remoraJ HTTP service uses port 7366 and will switch to next available (+1) if default is not available.

# Remora Control Commands 

## Get capabilities request

If the remora is working on localhost and default port you should query `localhost:7366/`

```
curl -XGET 'http://localhost:7366/'
```
 
to get a list of installed advices and available properties to change, each advice has a different set of properties.

## Change property request 

To change a property you run POST on remroaJ instance `localhost:7366/change`

i.e. to enable logging on JavaxServletAdvice you should run:

```
curl -XPOST -d '{
   	"advice": "JavaxServletAdvice",
   	"property": "logging",
   	"value": "false"
   }' 'http://localhost:7366/change'

```
  
  
# Available advices

As for version 0.1.6 there are such advices:

  "ApacheHttpClientAdvice",
  "ApacheLegacyHttpClientAdvice",
  "BankBenchmarkAdvice",
  "RemoraControlAdvice",
  "EjbRemoteAdvice",
  "HttpUrlConnectionAdvice",
  "JavaxServletAdvice",
  "JBossAdvice",
  "JBossServletAdvice",
  "JDBCConnectionAdvice",
  "JDBCStatementAdvice",
  "JDBCCallableStatementAdvice",
  "JMSCreateConnectionAdvice",
  "JMSReceiveAdvice",
  "JMSSendAdvice",
  "KafkaConsumerAdvice",
  "KafkaProducerAdvice",
  "KafkaConsumerClientAdvice",
  "SimpleTest",
  "SimpleTestConstructor",
  "SpringServiceAdvice",
  "SpringExceptionAdvice",
  "SpringTransactionAdvice",
  "WebLogicAdvice",
  "WebsocketSendAdvice",
  "WebsocketEndpointAdvice",
  "WebsocketReceiveAdvice",
  "WebsocketSessionAdvice",
  "IBMAdapterRSA",
  "WASAdvice"

# Common properties

Most of advices have properties:

* enabled - turn off/on advice
* logging - turn on/off logging

# Other properties & Sample response

```json
{
"version" : "0.1.6-SNAPSHOT-2020-04-14T14:54:12Z",
"vmid" : "17340@slabs-marius-PC",
"advices" : [	{
	"adviceName": "ApacheHttpClientAdvice",
	"properties": {
		"paramPrefix" : "PAR_",
		"headerCorrIDName" : "REMORA_CORR",
		"load" : "true",
		"extractParams" : "true",
		"logging" : "true",
		"enabled" : "true"
	}},
	{
	"adviceName": "ApacheLegacyHttpClientAdvice",
	"properties": {
		"headerCorrIDName" : "REMORA_CORR",
		"load" : "true",
		"logging" : "true",
		"enabled" : "true"
	}},
	{
	"adviceName": "BankBenchmarkAdvice",
	"properties": {
		"load" : "true",
		"logging" : "true",
		"enabled" : "true"
	}},
	{
	"adviceName": "RemoraControlAdvice",
	"properties": {
		"port" : "7366",
		"reporterSchedule" : "300",
		"adminURL" : "null"
	}},
	{
	"adviceName": "EjbRemoteAdvice",
	"properties": {
		"load" : "true",
		"logging" : "true",
		"enabled" : "true"
	}},
	{
	"adviceName": "HttpUrlConnectionAdvice",
	"properties": {
		"headerCorrIDName" : "REMORA_CORR",
		"load" : "false",
		"logging" : "true",
		"enabled" : "true"
	}},
	{
	"adviceName": "JavaxServletAdvice",
	"properties": {
		"headerCorrIDName" : "REMORA_CORR",
		"headerPrefix" : "HDR_",
		"load" : "true",
		"attachCorrelator" : "true",
		"logging" : "true",
		"enabled" : "true",
		"cookiePrefix" : "CKIE_"
	}},
	{
	"adviceName": "JBossAdvice",
	"properties": {
		"load" : "true",
		"logging" : "true",
		"enabled" : "true"
	}},
	{
	"adviceName": "JBossServletAdvice",
	"properties": {
		"load" : "true",
		"logging" : "true",
		"enabled" : "true"
	}},
	{
	"adviceName": "JDBCConnectionAdvice",
	"properties": {
		"load" : "true",
		"logging" : "true",
		"enabled" : "true"
	}},
	{
	"adviceName": "JDBCStatementAdvice",
	"properties": {
		"load" : "true",
		"logging" : "true",
		"enabled" : "true"
	}},
	{
	"adviceName": "JDBCCallableStatementAdvice",
	"properties": {
		"load" : "true",
		"parameterPrefix" : "PARAM_",
		"logging" : "true",
		"enabled" : "true"
	}},
	{
	"adviceName": "JMSCreateConnectionAdvice",
	"properties": {
		"load" : "true",
		"logging" : "true",
		"enabled" : "true"
	}},
	{
	"adviceName": "JMSReceiveAdvice",
	"properties": {
		"load" : "true",
		"logging" : "true",
		"enabled" : "true"
	}},
	{
	"adviceName": "JMSSendAdvice",
	"properties": {
		"load" : "true",
		"logging" : "true",
		"enabled" : "true"
	}},
	{
	"adviceName": "KafkaConsumerAdvice",
	"properties": {
		"load" : "true",
		"logging" : "true",
		"enabled" : "true"
	}},
	{
	"adviceName": "KafkaProducerAdvice",
	"properties": {
		"load" : "true",
		"logging" : "true",
		"enabled" : "true"
	}},
	{
	"adviceName": "KafkaConsumerClientAdvice",
	"properties": {
		"load" : "true",
		"logging" : "true",
		"enabled" : "true"
	}},
	{
	"adviceName": "SimpleTest",
	"properties": {
		"load" : "true",
		"logging" : "true",
		"enabled" : "true"
	}},
	{
	"adviceName": "SimpleTestConstructor",
	"properties": {
		"load" : "true",
		"logging" : "true",
		"enabled" : "true"
	}},
	{
	"adviceName": "SpringServiceAdvice",
	"properties": {
		"load" : "true",
		"logging" : "true",
		"enabled" : "true"
	}},
	{
	"adviceName": "SpringExceptionAdvice",
	"properties": {
		"load" : "true",
		"logging" : "true",
		"enabled" : "true"
	}},
	{
	"adviceName": "SpringTransactionAdvice",
	"properties": {
		"load" : "true",
		"logging" : "true",
		"enabled" : "true"
	}},
	{
	"adviceName": "WebLogicAdvice",
	"properties": {
		"load" : "true",
		"logging" : "true",
		"enabled" : "true"
	}},
	{
	"adviceName": "WebsocketSendAdvice",
	"properties": {
		"load" : "true",
		"logging" : "true",
		"enabled" : "true"
	}},
	{
	"adviceName": "WebsocketEndpointAdvice",
	"properties": {
		"load" : "true",
		"logging" : "true",
		"enabled" : "true"
	}},
	{
	"adviceName": "WebsocketReceiveAdvice",
	"properties": {
		"load" : "true",
		"logging" : "true",
		"enabled" : "true"
	}},
	{
	"adviceName": "WebsocketSessionAdvice",
	"properties": {
		"load" : "true",
		"logging" : "true",
		"enabled" : "true"
	}},
	{
	"adviceName": "IBMAdapterRSA",
	"properties": {
		"load" : "true",
		"logging" : "true",
		"enabled" : "true"
	}},
	{
	"adviceName": "WASAdvice",
	"properties": {
		"load" : "true",
		"logging" : "true",
		"enabled" : "true"
	}}
]
}

```

# Advice Statistics

To get statistics query `localhost:7366/stats/[advice name]`

```
curl -XGET 'localhost:7366/stats/JavaxServletAdvice'
```

Expected response:

```json
{
  "adviceName" : "JavaxServletAdvice",
  "invokeCount" : "2",
  "eventCreateCount" : "2",
  "errorCount": "0"
}
```


# Trace Output Statistics

To get statistics query `localhost:7366/queryStats`

```
curl -XGET 'localhost:7366/queryStats'
```

Expected response:

```json
{
  "intermediateQueueFailCount" : "0",
  "lastChronicleIndex" : "0",
  "chronicleErrorCount" : "0",
  "lastException": "null"
}
```
#ThreadDump

To get thradDump `localhost:7366/threadDump`

```
curl -XGET 'localhost:7366/threadDump'
```

Expected response:

```json
[
     {
       "ThreadName": "Monitor Ctrl-Break",
       "ThreadState": "RUNNABLE",
       "StackTrace": [
         "java.net.SocketInputStream.socketRead0(Native Method)",
         "java.net.SocketInputStream.socketRead(SocketInputStream.java:116)",
         "java.net.SocketInputStream.read(SocketInputStream.java:171)",
         "java.net.SocketInputStream.read(SocketInputStream.java:141)",
         "sun.nio.cs.StreamDecoder.readBytes(StreamDecoder.java:284)",
         "sun.nio.cs.StreamDecoder.implRead(StreamDecoder.java:326)",
         "sun.nio.cs.StreamDecoder.read(StreamDecoder.java:178)",
         "java.io.InputStreamReader.read(InputStreamReader.java:184)",
         "java.io.BufferedReader.fill(BufferedReader.java:161)",
         "java.io.BufferedReader.readLine(BufferedReader.java:324)",
         "java.io.BufferedReader.readLine(BufferedReader.java:389)",
         "com.intellij.rt.execution.application.AppMainV2$1.run(AppMainV2.java:64)"
       ]
     },
     {
       "ThreadName": "Attach Listener",
       "ThreadState": "RUNNABLE",
       "StackTrace": []
     },
     {
       "ThreadName": "Signal Dispatcher",
       "ThreadState": "RUNNABLE",
       "StackTrace": []
     },
     {
       "ThreadName": "Finalizer",
       "ThreadState": "WAITING",
       "StackTrace": [
         "java.lang.Object.wait(Native Method)",
         "java.lang.ref.ReferenceQueue.remove(ReferenceQueue.java:144)",
         "java.lang.ref.ReferenceQueue.remove(ReferenceQueue.java:165)",
         "java.lang.ref.Finalizer$FinalizerThread.run(Finalizer.java:216)"
       ]
     },
     {
       "ThreadName": "Reference Handler",
       "ThreadState": "WAITING",
       "StackTrace": [
         "java.lang.Object.wait(Native Method)",
         "java.lang.Object.wait(Object.java:502)",
         "java.lang.ref.Reference.tryHandlePending(Reference.java:191)",
         "java.lang.ref.Reference$ReferenceHandler.run(Reference.java:153)"
       ]
     },
     {
       "ThreadName": "main",
       "ThreadState": "RUNNABLE",
       "StackTrace": [
         "sun.management.ThreadImpl.dumpThreads0(Native Method)",
         "sun.management.ThreadImpl.dumpAllThreads(ThreadImpl.java:454)",
         "com.jkoolcloud.remora.takes.TkThreadDump.act(TkThreadDump.java:47)",
         "com.jkoolcloud.remora.takes.TkThreadDumpTest.testThreadDumpResponse(TkThreadDumpTest.java:31)",
         "sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)",
         "sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)",
         "sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)",
         "java.lang.reflect.Method.invoke(Method.java:498)",
         "org.junit.runners.model.FrameworkMethod$1.runReflectiveCall(FrameworkMethod.java:59)",
         "org.junit.internal.runners.model.ReflectiveCallable.run(ReflectiveCallable.java:12)",
         "org.junit.runners.model.FrameworkMethod.invokeExplosively(FrameworkMethod.java:56)",
         "org.junit.internal.runners.statements.InvokeMethod.evaluate(InvokeMethod.java:17)",
         "org.junit.runners.ParentRunner$3.evaluate(ParentRunner.java:306)",
         "org.junit.runners.BlockJUnit4ClassRunner$1.evaluate(BlockJUnit4ClassRunner.java:100)",
         "org.junit.runners.ParentRunner.runLeaf(ParentRunner.java:366)",
         "org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:103)",
         "org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:63)",
         "org.junit.runners.ParentRunner$4.run(ParentRunner.java:331)",
         "org.junit.runners.ParentRunner$1.schedule(ParentRunner.java:79)",
         "org.junit.runners.ParentRunner.runChildren(ParentRunner.java:329)",
         "org.junit.runners.ParentRunner.access$100(ParentRunner.java:66)",
         "org.junit.runners.ParentRunner$2.evaluate(ParentRunner.java:293)",
         "org.junit.runners.ParentRunner$3.evaluate(ParentRunner.java:306)",
         "org.junit.runners.ParentRunner.run(ParentRunner.java:413)",
         "org.junit.runner.JUnitCore.run(JUnitCore.java:137)",
         "com.intellij.junit4.JUnit4IdeaTestRunner.startRunnerWithArgs(JUnit4IdeaTestRunner.java:68)",
         "com.intellij.rt.junit.IdeaTestRunner$Repeater.startRunnerWithArgs(IdeaTestRunner.java:33)",
         "com.intellij.rt.junit.JUnitStarter.prepareStreamsAndStart(JUnitStarter.java:230)",
         "com.intellij.rt.junit.JUnitStarter.main(JUnitStarter.java:58)"
       ]
     }
   ]
```


# Capturing Stack Straces

You can enable stack traces of instrumented method calls by enabling `sendStackTrace` property on any advice.
It is global property and will affect all generated events. 

```
curl -XPOST -d '{
   	"advice": "JavaxServletAdvice",
   	"property": "sendStackTrace",
   	"value": "true"
   }' 'http://localhost:7366/change'

```
