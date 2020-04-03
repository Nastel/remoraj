# Remora control 

Remora control plugin enables you to control remoraJ  java agent on demand locally or remotely.
Remora control uses REST for communication.
By default it will work on port 7366. If the port is already used it will switch to next available.

# Requests 

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

As for version 0.1.5 there are such advices:

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

```
{
"remoraJVersion" : "0.1.5-SNAPSHOT-2020-04-03T10:47:28Z",
"vmIdentification" : "null",
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
]



# Statistics

To get statistics query `localhost:7366/statistics/[advice name]`

```
curl -XGET 'localhost:7366/statistics/JavaxServletAdvice'
```

Expected response:

```
{
  "adviceName" : "JavaxServletAdvice",
  "invokeCount" : "2",
  "eventCreateCount" : "2",
  "errorCount": "0"
}
```


# QueueStatistics

To get statistics query `localhost:7366/queryStatistics`

```
curl -XGET 'localhost:7366/queryStatistics'
```

Expected response:

```
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