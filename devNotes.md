# Introduction

RemoraJ is a modular project each module represents single service (i.e. JSM, HTPP, KAFKA) 
interception. Each module is built as separate JAR in "modules" folder, this will enable to
quickly loading/unloading necessary modules, without a need to change configuration, or make custom 
builds for specific needs. 



# Interception

## Method interception; Entry, Exit and EntryDefinition
Each intercepted call is intercepted on begin of method code and at the end of method code.
At the method call beginning RemoraJ creates Entry entity, at the end it creates Exit entity, both 
are serialized to the Queue. The method call wrapped with EntryDefinition witch one is not serialised, 
but handles the correlation between later two.

Entry and Exit shares the same GUID, and it's supposed to be merged as one in data collector and UI side.
The main justification to send two Entries for a single method call to the Queue is to "catch" locking methods. 

## CallStack 

Subsequent (i.e. Http request invokes Database and JMS call) call's are handled as CallStack. CallStack is also 
responsible for propagating common fields i.e. server or application.

## InterceptingWrapper classes

Most of the interceptions are Generic and instrumented based on Interfaces or Annotations to support multiple 
implementations (JMS, JDBC etc., JavaX server), thus introducing a problem there multiple wrapper classes introduces 
unwanted interception and data generation. RemoraJ will instrument all such wrapper classes implementing service Interfaces, 
but it would not create additional EntryDefinitions for Ech one, instead reusing first created. Some of the data supposed to be 
collected in Exit properties would be duplicated, as can be collected in multiple wrapping Classes. For a class name the last one 
is reported, other, suppressed ones, would appear as Exit.properties as "SClass" entries.


  
## Data model

entry: {
  id: b4a0f552-5ecf-11ea-a5f9-4ccc6a3575da,
  v: !byte 1,
  mode: RUNNING,
  adviceClass: TESTAdvice,
  startTime: 123456789,
  name: TESTname,
  clazz: EntryClass,
  stackTrace: !!null "",
  vmIdentification: Junit,
  thread: JunitThread
}


exit: {
  id: "38aad716-5ed0-11ea-abd7-4ccc6a3575da",
  v: !byte 1,
  name: TESTName,
  mode: STOP,
  resource: "http://localhost/test",
  resourceType: NETADDR,
  application: JUnit,
  properties: {
    Property  4: TEST4,
    Property  3: TEST3,
    Property  2: TEST2,
    Property  1: TEST1
  },
  eventType: CALL,
  exception: !!null "",
  correlator: !!null "",
  exceptionTrace: !!null ""
}

## Properties and configuration

Each advice field annotated with `@RemoraConfig.Configurable` can be changed either by configuration file `remora.properties` or "remora-control" plugin.
These properties should be static as advices intercept methods are static.
Common properties i.e. "sendStackTrace" is in base advice class, these properties are *not static*. State of advice is managed in `AdviceManager` instance.


