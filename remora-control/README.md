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
"version" : "0.1.7-SNAPSHOT-2020-04-24T13:24:44Z",
"vmid" : "13008@slabs-marius-PC",
"advices" : [	{
	"adviceName": "ApacheHttpClientAdvice",
	"properties": {
		"paramPrefix" : "PAR_",
		"headerCorrIDName" : "REMORA_CORR",
		"maxStackTraceElements" : "30",
		"sendStackTrace" : "false",
		"checkLastPropertyValue" : "true",
		"load" : "true",
		"extractParams" : "true",
		"logging" : "true",
		"enabled" : "true"
	}},
	{
	"adviceName": "ApacheLegacyHttpClientAdvice",
	"properties": {
		"headerCorrIDName" : "REMORA_CORR",
		"maxStackTraceElements" : "30",
		"sendStackTrace" : "false",
		"checkLastPropertyValue" : "true",
		"load" : "true",
		"logging" : "true",
		"enabled" : "true"
	}},
	{
	"adviceName": "BankBenchmarkAdvice",
	"properties": {
		"maxStackTraceElements" : "30",
		"sendStackTrace" : "false",
		"checkLastPropertyValue" : "true",
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
		"maxStackTraceElements" : "30",
		"sendStackTrace" : "false",
		"checkLastPropertyValue" : "true",
		"load" : "true",
		"logging" : "true",
		"enabled" : "true"
	}},
	{
	"adviceName": "HttpUrlConnectionAdvice",
	"properties": {
		"headerCorrIDName" : "REMORA_CORR",
		"maxStackTraceElements" : "30",
		"sendStackTrace" : "false",
		"checkLastPropertyValue" : "true",
		"load" : "false",
		"logging" : "true",
		"enabled" : "true"
	}},
	{
	"adviceName": "JavaxServletAdvice",
	"properties": {
		"headerCorrIDName" : "REMORA_CORR",
		"maxStackTraceElements" : "30",
		"sendStackTrace" : "false",
		"checkLastPropertyValue" : "true",
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
		"maxStackTraceElements" : "30",
		"sendStackTrace" : "false",
		"checkLastPropertyValue" : "true",
		"load" : "true",
		"logging" : "true",
		"enabled" : "true"
	}},
	{
	"adviceName": "JBossServletAdvice",
	"properties": {
		"maxStackTraceElements" : "30",
		"sendStackTrace" : "false",
		"checkLastPropertyValue" : "true",
		"load" : "true",
		"logging" : "true",
		"enabled" : "true"
	}},
	{
	"adviceName": "JDBCConnectionAdvice",
	"properties": {
		"maxStackTraceElements" : "30",
		"sendStackTrace" : "false",
		"checkLastPropertyValue" : "true",
		"load" : "true",
		"logging" : "true",
		"enabled" : "true"
	}},
	{
	"adviceName": "JDBCStatementAdvice",
	"properties": {
		"maxStackTraceElements" : "30",
		"sendStackTrace" : "false",
		"checkLastPropertyValue" : "true",
		"load" : "true",
		"logging" : "true",
		"enabled" : "true"
	}},
	{
	"adviceName": "JDBCCallableStatementAdvice",
	"properties": {
		"maxStackTraceElements" : "30",
		"sendStackTrace" : "false",
		"checkLastPropertyValue" : "true",
		"load" : "true",
		"parameterPrefix" : "PARAM_",
		"logging" : "true",
		"enabled" : "true"
	}},
	{
	"adviceName": "JMSCreateConnectionAdvice",
	"properties": {
		"maxStackTraceElements" : "30",
		"sendStackTrace" : "false",
		"checkLastPropertyValue" : "true",
		"load" : "true",
		"logging" : "true",
		"enabled" : "true"
	}},
	{
	"adviceName": "JMSReceiveAdvice",
	"properties": {
		"maxStackTraceElements" : "30",
		"sendStackTrace" : "false",
		"checkLastPropertyValue" : "true",
		"load" : "true",
		"fetchMsg" : "false",
		"logging" : "true",
		"enabled" : "true"
	}},
	{
	"adviceName": "JMSSendAdvice",
	"properties": {
		"maxStackTraceElements" : "30",
		"sendStackTrace" : "false",
		"checkLastPropertyValue" : "true",
		"load" : "true",
		"fetchMsg" : "false",
		"logging" : "true",
		"enabled" : "true"
	}},
	{
	"adviceName": "KafkaConsumerAdvice",
	"properties": {
		"maxStackTraceElements" : "30",
		"sendStackTrace" : "false",
		"checkLastPropertyValue" : "true",
		"load" : "true",
		"logging" : "true",
		"enabled" : "true"
	}},
	{
	"adviceName": "KafkaProducerAdvice",
	"properties": {
		"maxStackTraceElements" : "30",
		"sendStackTrace" : "false",
		"checkLastPropertyValue" : "true",
		"load" : "true",
		"logging" : "true",
		"enabled" : "true"
	}},
	{
	"adviceName": "KafkaConsumerClientAdvice",
	"properties": {
		"maxStackTraceElements" : "30",
		"sendStackTrace" : "false",
		"checkLastPropertyValue" : "true",
		"load" : "true",
		"logging" : "true",
		"enabled" : "true"
	}},
	{
	"adviceName": "SimpleTest",
	"properties": {
		"maxStackTraceElements" : "30",
		"sendStackTrace" : "false",
		"checkLastPropertyValue" : "true",
		"load" : "true",
		"logging" : "true",
		"enabled" : "true"
	}},
	{
	"adviceName": "SimpleTestConstructor",
	"properties": {
		"maxStackTraceElements" : "30",
		"sendStackTrace" : "false",
		"checkLastPropertyValue" : "true",
		"load" : "true",
		"logging" : "true",
		"enabled" : "true"
	}},
	{
	"adviceName": "SpringServiceAdvice",
	"properties": {
		"maxStackTraceElements" : "30",
		"sendStackTrace" : "false",
		"checkLastPropertyValue" : "true",
		"load" : "true",
		"logging" : "true",
		"enabled" : "true"
	}},
	{
	"adviceName": "SpringExceptionAdvice",
	"properties": {
		"maxStackTraceElements" : "30",
		"sendStackTrace" : "false",
		"checkLastPropertyValue" : "true",
		"load" : "true",
		"logging" : "true",
		"enabled" : "true"
	}},
	{
	"adviceName": "SpringTransactionAdvice",
	"properties": {
		"maxStackTraceElements" : "30",
		"sendStackTrace" : "false",
		"checkLastPropertyValue" : "true",
		"load" : "true",
		"logging" : "true",
		"enabled" : "true"
	}},
	{
	"adviceName": "WebLogicAdvice",
	"properties": {
		"maxStackTraceElements" : "30",
		"sendStackTrace" : "false",
		"checkLastPropertyValue" : "true",
		"load" : "true",
		"logging" : "true",
		"enabled" : "true"
	}},
	{
	"adviceName": "WebsocketSendAdvice",
	"properties": {
		"maxStackTraceElements" : "30",
		"sendStackTrace" : "false",
		"checkLastPropertyValue" : "true",
		"load" : "true",
		"logging" : "true",
		"enabled" : "true"
	}},
	{
	"adviceName": "WebsocketEndpointAdvice",
	"properties": {
		"maxStackTraceElements" : "30",
		"sendStackTrace" : "false",
		"checkLastPropertyValue" : "true",
		"load" : "true",
		"logging" : "true",
		"enabled" : "true"
	}},
	{
	"adviceName": "WebsocketReceiveAdvice",
	"properties": {
		"maxStackTraceElements" : "30",
		"sendStackTrace" : "false",
		"checkLastPropertyValue" : "true",
		"load" : "true",
		"logging" : "true",
		"enabled" : "true"
	}},
	{
	"adviceName": "WebsocketSessionAdvice",
	"properties": {
		"maxStackTraceElements" : "30",
		"sendStackTrace" : "false",
		"checkLastPropertyValue" : "true",
		"load" : "true",
		"logging" : "true",
		"enabled" : "true"
	}},
	{
	"adviceName": "IBMAdapterRSA",
	"properties": {
		"maxStackTraceElements" : "30",
		"sendStackTrace" : "false",
		"checkLastPropertyValue" : "true",
		"load" : "true",
		"logging" : "true",
		"enabled" : "true"
	}},
	{
	"adviceName": "WASAdvice",
	"properties": {
		"maxStackTraceElements" : "30",
		"sendStackTrace" : "false",
		"checkLastPropertyValue" : "true",
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
  "memQErrorCount" : "0",
  "lastPersistQIndex" : "0",
  "persistQErrorCount" : "0",
  "lastException": "null",
  "usableSpace": "12 754 915 328"
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

```
curl -XPOST -d '{
   	"advice": "JavaxServletAdvice",
   	"property": "sendStackTrace",
   	"value": "true"
   }' 'http://localhost:7366/change'

```

# GC and memory Memory info 

To get memory and grabage collector(s) info get `localhost:7366/gcInfo`

```
curl -XGET 'localhost:7366/gcInfo'
```

Expected response:

```
{
  "Heap": {
		  "Init": "2 147 483 648",
		  "Used": "352 039 392",
		  "Max": "2 099 773 440",
		  "Commited": "2 099 773 440"}
		,
  "NonHeap": {
		  "Init": "2 555 904",
		  "Used": "186 918 480",
		  "Max": "1 593 835 520",
		  "Commited": "204 472 320"}
		,
  "GC": [{
		  "Name": "PS Scavenge",
		  "Collections": "68",
		  "LastCollectionTime": 1 747,
		  "PoolNames": ["PS Eden Space","PS Survivor Space"]
		}
		,{
		  "Name": "PS MarkSweep",
		  "Collections": "1",
		  "LastCollectionTime": 339,
		  "PoolNames": ["PS Eden Space","PS Survivor Space","PS Old Gen"]
		}
		],
  "Details": [{
		  "Name": "Code Cache",
		  "Type": "Non-heap memory",
		  "Used": "N/A",
		  "Collections": {
				  "Init": "2 555 904",
				  "Used": "33 144 064",
				  "Max": "251 658 240",
				  "Commited": "33 554 432"}
				
		}
		,{
		  "Name": "Metaspace",
		  "Type": "Non-heap memory",
		  "Used": "N/A",
		  "Collections": {
				  "Init": "0",
				  "Used": "135 530 400",
				  "Max": "268 435 456",
				  "Commited": "148 897 792"}
				
		}
		,{
		  "Name": "Compressed Class Space",
		  "Type": "Non-heap memory",
		  "Used": "N/A",
		  "Collections": {
				  "Init": "0",
				  "Used": "18 324 256",
				  "Max": "1 073 741 824",
				  "Commited": "22 020 096"}
				
		}
		,{
		  "Name": "PS Eden Space",
		  "Type": "Heap memory",
		  "Used": {
				  "Init": "537 395 200",
				  "Used": "0",
				  "Max": "620 232 704",
				  "Commited": "620 232 704"}
				,
		  "Collections": {
				  "Init": "537 395 200",
				  "Used": "629 669 888",
				  "Max": "643 301 376",
				  "Commited": "629 669 888"}
				
		}
		,{
		  "Name": "PS Survivor Space",
		  "Type": "Heap memory",
		  "Used": {
				  "Init": "89 128 960",
				  "Used": "35 048 792",
				  "Max": "47 710 208",
				  "Commited": "47 710 208"}
				,
		  "Collections": {
				  "Init": "89 128 960",
				  "Used": "58 173 592",
				  "Max": "89 128 960",
				  "Commited": "89 128 960"}
				
		}
		,{
		  "Name": "PS Old Gen",
		  "Type": "Heap memory",
		  "Used": {
				  "Init": "1 431 830 528",
				  "Used": "120 501 184",
				  "Max": "1 431 830 528",
				  "Commited": "1 431 830 528"}
				,
		  "Collections": {
				  "Init": "1 431 830 528",
				  "Used": "161 871 376",
				  "Max": "1 431 830 528",
				  "Commited": "1 431 830 528"}
				
		}
		]
}
```