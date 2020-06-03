# Remora Control 

Remora control plugin enables control of remoraJ Java Agent on demand over HTTP/REST.
By default remoraJ HTTP service uses port 7366 and will switch to next available (+1) if default port is not available.

# Remora Control Commands 

## Get Capabilities Request

If the remora is working on localhost and default port you should query `localhost:7366/`

```
curl -XGET 'http://localhost:7366/'
```
 
to get a list of installed advices and available properties to change, each advice has a different set of properties.

## Change Property Request 

To change a property you run POST on remroaJ instance `localhost:7366/change`

i.e. to enable logging on `JavaxServletAdvice` you should run:

```
curl -XPOST -d '{
   	"advice": "JavaxServletAdvice",
   	"property": "logging",
   	"value": "false"
   }' 'http://localhost:7366/change'

``` 
  
# Available Advices

Below is a list of available advices:
```
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
```

# Common Advice Properties

* `enabled` - turn off/on advice
* `logging` - turn on/off logging

# Other properties & Sample response

```json
{
"version" : "0.1.7-SNAPSHOT-2020-05-07T11:37:51Z",
"vmid" : "17800@slabs-marius-PC",
"advices" : [	{
	"adviceName": "ApacheHttpClientAdvice",
	"properties": {
		"paramPrefix" : "PAR_",
		"headerCorrIDName" : "REMORA_CORR",
		"maxStackTraceElements" :  30,
		"sendStackTrace" : false,
		"checkCallRepeats" : true,
		"extractParams" : true,
		"logging" : true,
		"enabled" : true
	}},
	{
	"adviceName": "ApacheLegacyHttpClientAdvice",
	"properties": {
		"headerCorrIDName" : "REMORA_CORR",
		"maxStackTraceElements" :  30,
		"sendStackTrace" : false,
		"checkCallRepeats" : true,
		"logging" : true,
		"enabled" : true
	}},
	{
	"adviceName": "BankBenchmarkAdvice",
	"properties": {
		"maxStackTraceElements" :  30,
		"sendStackTrace" : false,
		"checkCallRepeats" : true,
		"logging" : true,
		"enabled" : true
	}},
	{
	"adviceName": "RemoraControlAdvice",
	"properties": {
		"port" :  7366,
		"heapDumpPath" : "/opt/remora/dumps/",
		"reporterSchedule" :  300,
		"adminURL" : "null"
	}},
	{
	"adviceName": "EjbRemoteAdvice",
	"properties": {
		"maxStackTraceElements" :  30,
		"sendStackTrace" : false,
		"checkCallRepeats" : true,
		"logging" : true,
		"enabled" : true
	}},
	{
	"adviceName": "HttpUrlConnectionAdvice",
	"properties": {
		"headerCorrIDName" : "REMORA_CORR",
		"maxStackTraceElements" :  30,
		"sendStackTrace" : false,
		"checkCallRepeats" : true,
		"logging" : true,
		"enabled" : true
	}},
	{
	"adviceName": "JavaxServletAdvice",
	"properties": {
		"headerCorrIDName" : "REMORA_CORR",
		"maxStackTraceElements" :  30,
		"sendStackTrace" : false,
		"checkCallRepeats" : true,
		"headerPrefix" : "HDR_",
		"attachCorrelator" : true,
		"logging" : true,
		"enabled" : true,
		"cookiePrefix" : "CKIE_"
	}},
	{
	"adviceName": "JBossAdvice",
	"properties": {
		"maxStackTraceElements" :  30,
		"sendStackTrace" : false,
		"checkCallRepeats" : true,
		"logging" : true,
		"enabled" : true
	}},
	{
	"adviceName": "JBossServletAdvice",
	"properties": {
		"maxStackTraceElements" :  30,
		"sendStackTrace" : false,
		"checkCallRepeats" : true,
		"logging" : true,
		"enabled" : true
	}},
	{
	"adviceName": "JDBCConnectionAdvice",
	"properties": {
		"maxStackTraceElements" :  30,
		"sendStackTrace" : false,
		"checkCallRepeats" : true,
		"logging" : true,
		"enabled" : true
	}},
	{
	"adviceName": "JDBCStatementAdvice",
	"properties": {
		"maxStackTraceElements" :  30,
		"sendStackTrace" : false,
		"checkCallRepeats" : true,
		"logging" : true,
		"enabled" : true
	}},
	{
	"adviceName": "JDBCCallableStatementAdvice",
	"properties": {
		"maxStackTraceElements" :  30,
		"sendStackTrace" : false,
		"checkCallRepeats" : true,
		"parameterPrefix" : "PARAM_",
		"logging" : true,
		"enabled" : true
	}},
	{
	"adviceName": "JMSCreateConnectionAdvice",
	"properties": {
		"maxStackTraceElements" :  30,
		"sendStackTrace" : false,
		"checkCallRepeats" : true,
		"logging" : true,
		"enabled" : true
	}},
	{
	"adviceName": "JMSReceiveAdvice",
	"properties": {
		"maxStackTraceElements" :  30,
		"sendStackTrace" : false,
		"checkCallRepeats" : true,
		"fetchMsg" : false,
		"logging" : true,
		"enabled" : true
	}},
	{
	"adviceName": "JMSSendAdvice",
	"properties": {
		"maxStackTraceElements" :  30,
		"sendStackTrace" : false,
		"checkCallRepeats" : true,
		"fetchMsg" : false,
		"logging" : true,
		"enabled" : true
	}},
	{
	"adviceName": "KafkaConsumerAdvice",
	"properties": {
		"maxStackTraceElements" :  30,
		"sendStackTrace" : false,
		"checkCallRepeats" : true,
		"logging" : true,
		"enabled" : true
	}},
	{
	"adviceName": "KafkaProducerAdvice",
	"properties": {
		"maxStackTraceElements" :  30,
		"sendStackTrace" : false,
		"checkCallRepeats" : true,
		"logging" : true,
		"enabled" : true
	}},
	{
	"adviceName": "KafkaConsumerClientAdvice",
	"properties": {
		"maxStackTraceElements" :  30,
		"sendStackTrace" : false,
		"checkCallRepeats" : true,
		"logging" : true,
		"enabled" : true
	}},
	{
	"adviceName": "SimpleTest",
	"properties": {
		"maxStackTraceElements" :  30,
		"sendStackTrace" : false,
		"checkCallRepeats" : true,
		"logging" : true,
		"enabled" : true
	}},
	{
	"adviceName": "SimpleTestConstructor",
	"properties": {
		"maxStackTraceElements" :  30,
		"sendStackTrace" : false,
		"checkCallRepeats" : true,
		"logging" : true,
		"enabled" : true
	}},
	{
	"adviceName": "SpringServiceAdvice",
	"properties": {
		"maxStackTraceElements" :  30,
		"sendStackTrace" : false,
		"checkCallRepeats" : true,
		"logging" : true,
		"enabled" : true
	}},
	{
	"adviceName": "SpringExceptionAdvice",
	"properties": {
		"maxStackTraceElements" :  30,
		"sendStackTrace" : false,
		"checkCallRepeats" : true,
		"logging" : true,
		"enabled" : true
	}},
	{
	"adviceName": "SpringTransactionAdvice",
	"properties": {
		"maxStackTraceElements" :  30,
		"sendStackTrace" : false,
		"checkCallRepeats" : true,
		"logging" : true,
		"enabled" : true
	}},
	{
	"adviceName": "WebLogicAdvice",
	"properties": {
		"maxStackTraceElements" :  30,
		"sendStackTrace" : false,
		"checkCallRepeats" : true,
		"logging" : true,
		"enabled" : true
	}},
	{
	"adviceName": "WebsocketSendAdvice",
	"properties": {
		"maxStackTraceElements" :  30,
		"sendStackTrace" : false,
		"checkCallRepeats" : true,
		"logging" : true,
		"enabled" : true
	}},
	{
	"adviceName": "WebsocketEndpointAdvice",
	"properties": {
		"maxStackTraceElements" :  30,
		"sendStackTrace" : false,
		"checkCallRepeats" : true,
		"logging" : true,
		"enabled" : true
	}},
	{
	"adviceName": "WebsocketReceiveAdvice",
	"properties": {
		"maxStackTraceElements" :  30,
		"sendStackTrace" : false,
		"checkCallRepeats" : true,
		"logging" : true,
		"enabled" : true
	}},
	{
	"adviceName": "WebsocketSessionAdvice",
	"properties": {
		"maxStackTraceElements" :  30,
		"sendStackTrace" : false,
		"checkCallRepeats" : true,
		"logging" : true,
		"enabled" : true
	}},
	{
	"adviceName": "IBMAdapterRSA",
	"properties": {
		"maxStackTraceElements" :  30,
		"sendStackTrace" : false,
		"checkCallRepeats" : true,
		"logging" : true,
		"enabled" : true
	}},
	{
	"adviceName": "WASAdvice",
	"properties": {
		"maxStackTraceElements" :  30,
		"sendStackTrace" : false,
		"checkCallRepeats" : true,
		"logging" : true,
		"enabled" : true
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
  "invokeCount" : 3,
  "eventCreateCount" : 3,
  "errorCount": 0
}
```


# Traces Output Statistics

To get statistics query `localhost:7366/queryStats`

```
curl -XGET 'localhost:7366/queryStats'
```

Expected response:

```json
{
  "memQErrorCount" : 0,
  "lastPersistQIndex" : 0,
  "persistQErrorCount" : 0,
  "lastException": "null",
  "usableSpace":  10719305728
}
```
# ThreadDump

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

# GC and Memory Memory Info 

To get memory and grabage collector(s) info get `localhost:7366/gcInfo`

```
curl -XGET 'localhost:7366/gcInfo'
```

Expected response:

```json
{
  "Heap": {
		  "Init": 2147483648,
		  "Used": 612181928,
		  "Max": 2099249152,
		  "Commited": 2099249152}
		,
  "NonHeap": {
		  "Init": 2555904,
		  "Used": 188445752,
		  "Max": 1593835520,
		  "Commited": 205914112}
		,
  "GC": [{
		  "Name": "PS Scavenge",
		  "Collections": 68,
		  "LastCollectionTime": 1730,
		  "PoolNames": ["PS Eden Space","PS Survivor Space"]
		}
		,{
		  "Name": "PS MarkSweep",
		  "Collections": 1,
		  "LastCollectionTime": 678,
		  "PoolNames": ["PS Eden Space","PS Survivor Space","PS Old Gen"]
		}
		],
  "Details": [{
		  "Name": "Code Cache",
		  "Type": "Non-heap memory",
		  "Used": "N/A",
		  "Collections": {
				  "Init": 2555904,
				  "Used": 34006720,
				  "Max": 251658240,
				  "Commited": 34471936}
				
		}
		,{
		  "Name": "Metaspace",
		  "Type": "Non-heap memory",
		  "Used": "N/A",
		  "Collections": {
				  "Init": 0,
				  "Used": 136204616,
				  "Max": 268435456,
				  "Commited": 149422080}
				
		}
		,{
		  "Name": "Compressed Class Space",
		  "Type": "Non-heap memory",
		  "Used": "N/A",
		  "Collections": {
				  "Init": 0,
				  "Used": 18302008,
				  "Max": 1073741824,
				  "Commited": 22020096}
				
		}
		,{
		  "Name": "PS Eden Space",
		  "Type": "Heap memory",
		  "Used": {
				  "Init": 537395200,
				  "Used": 0,
				  "Max": 619184128,
				  "Commited": 619184128}
				,
		  "Collections": {
				  "Init": 537395200,
				  "Used": 627572736,
				  "Max": 640679936,
				  "Commited": 627572736}
				
		}
		,{
		  "Name": "PS Survivor Space",
		  "Type": "Heap memory",
		  "Used": {
				  "Init": 89128960,
				  "Used": 35108544,
				  "Max": 48234496,
				  "Commited": 48234496}
				,
		  "Collections": {
				  "Init": 89128960,
				  "Used": 68936656,
				  "Max": 89128960,
				  "Commited": 89128960}
				
		}
		,{
		  "Name": "PS Old Gen",
		  "Type": "Heap memory",
		  "Used": {
				  "Init": 1431830528,
				  "Used": 129796952,
				  "Max": 1431830528,
				  "Commited": 1431830528}
				,
		  "Collections": {
				  "Init": 1431830528,
				  "Used": 178849320,
				  "Max": 1431830528,
				  "Commited": 1431830528}
				
		}
		]
}
```

# System and CPU Info

to get Systems and processor info get `localhost:7366/sysInfo`.

```
curl -XGET 'localhost:7366/sysInfo'
```

Expected response:
```json
{
	"OsName" : "Windows 10",
	"OsArch" : "amd64",
	"AvailableProcessors" : 8,
	"OsVersion" : "10.0",
	"CommittedVirtualMemorySize" :  2659962880,
	"FreePhysicalMemorySize" :  1209249792,
	"FreeSwapSpaceSize" :  9557762048,
	"ProcessCpuLoad" :  0,
	"ProcessCpuTime" :  228062500000,
	"SystemCpuLoad" :  0.34,
	"TotalPhysicalMemorySize" :  17063899136,
	"TotalSwapSpaceSize" :  35314622464
}
```

#Filters 

## Filter info 

to get filters info get `localhost:7366/filters`

```
curl -XGET 'localhost:7366/filters'
```

Expected response:
```json
[
{
  "filterName" : "ingnoredStreams",
  "filterClass" : "class com.jkoolcloud.remora.filters.ClassNameFilter",
  "properties" : {"mode" : "EXCLUDE",
				"regex" : false,
				"classNames" : ["java.net.SocketInputStream","java.util.jar.JarVerifier$VerifierStream"]
				}
},
{
  "filterName" : "ingnoredMysqlStreams",
  "filterClass" : "class com.jkoolcloud.remora.filters.ClassNameFilter",
  "properties" : {"mode" : "EXCLUDE",
				"regex" : true,
				"classNames" : ["com\.mysql.*"]
				}
}
]
```

## New filter

to create new filter POST `localhost:7366/filters` with body:

```
{
	"class": "com.jkoolcloud.remora.filters.ClassNameFilter",
	"name": "test",
	"regex": "false",
	"classNames": "com.test;com.test2",
	"mode": "EXCLUDE"
}
```

i.e.:


```
curl -XPOST -d '{
                	"class": "com.jkoolcloud.remora.filters.ClassNameFilter",
                	"name": "test",
                	"regex": "false",
                	"classNames": "com.test;com.test2",
                	"mode": "EXCLUDE"
                }' 'http://localhost:7366/filters'

```

class - fully qualified filter path
name - filter name
mode - filter mode INCLUDE/EXCLUDE, exclude will filter out defined interception, include will only pass defined interception
other options - filter dependent


`com.jkoolcloud.remora.filters.ClassNameFilter` filter options:

classNames = list of class names ';' (semicolon separated) 
regex -  true/false, is true regex is applied, class name must match whole pattern, backslash should be escaped with another backslash i.e. "com\\.mysql.*"

## Apply filter for a advice

see [Change property request section](##-Change-Property-Request) 
    
i.e. to add filter "test" on `JavaxServletAdvice` you should run:
    
```
curl -XPOST -d '{
   	"advice": "JavaxServletAdvice",
   	"property": "filters",
   	"value": "test"
   }' 'http://localhost:7366/change'

```  
Filters will be overridden, you must include all filter you want to apply.
