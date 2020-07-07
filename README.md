
# RemoraJ: Extensible Java Bytecode Agent for Optimizing Java App Performance

RemoraJ is an extensible java profiling agent which uses bytecode instrumentation to intercept java IPC calls with minimal overhead. RemoraJ monitors java apps by tracking IPC calls such as: HTTP, WebServices, JMS, JDBC, Sockets, WebSockets, Kafka, I/O Streams and more. The goal is to capture inter-JVM calls, I/O, messages, exceptions, timings which are valuable during troubleshooting and performance optimization of java apps. 

RemoraJ consists of two main runtime components:

* `Remora Java Agent`: loaded into JVM, app server, instruments bytecode, emits traces and metrics via pluggable advices
* `Remora Streams Agent`: forwards the traces generated by the java agent to one of the supported analytics platforms below

**Platforms for Analyzing RemoraJ Traces**
* [jKool](https://www.jkoolcloud.com/) -- SaaS platform for analyzing high velocity machine data
* [Nastel XRay](https://xray.nastel.com/xray/Nastel/login.jsp) -- SaaS Platform for AIOps & transaction tracking
* Integrate traces into ELK, Splunk or other platform via [agent output class implementation](#agent-trace-output)

Both agents communicate via a memory mapped queue stored on a filesystem see `tnt-data-source.xml` for details. The Streams Agent sends traces using JSON over HTTPS.

**NOTE:** Streams Agent is based on [tnt4j-streams](https://github.com/Nastel/tnt4j-streams) project designed to process, parse and stream time series data over a number of transports such as: HTTPS, Kafka, JMS, MQTT, MQ, files, etc.

![RemoraJ Architecture](https://github.com/Nastel/remoraj/blob/master/remoraj-arch.png)

## RemoraJ Intercepts (Advices)
RemoraJ supports the following intercepts (advices):
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
# Installing RemoraJ

You must configure the Remora Java agent and Streams agent to run RemoraJ. Your should have a working data repository and a streaming access token. Below is the outline of RemoraJ setup:

* Configure the Remora Java Agent for your JVM (captures java call traces, metrics)
* Start your application or app server (with remora agent)

If you decide to send traces to Nastel XRay or jKool please complete the following steps:
* Obtain your account with [jKool](https://www.jkoolcloud.com/) or [Nastel XRay](https://www.nastel.com/nastel-xray/). Make sure to get your streaming access token
* Configure the Streams Agent (you will need your streaming access token here)
* Start the Streams Agent (forwards traces to your data repository associated with your streaming access token)

Login to your dashboard to view & analyze results: 
* [jKool Dashboard](https://jkool.jkoolcloud.com/jKool/login.jsp)
* [Nastel XRay Dashboard](https://xray.nastel.com/xray/Nastel/login.jsp)

## Prerequisites

Java >8 runtime (IBM WebSphere >8.5.5, Tomcat >8, Jboss >7.2 should be already running Java 8. Make sure JVM is Java 1.8 or above).

The following jar files are required to build `remora-websphere` advices:
* `com.ibm.jaxws.thinclient_8.5.0.jar`
* `com.ibm.ws.runtime.jar`
* `com.ibm.ws.webcontainer.jar`
* `rsahelpers.jar`
* `com.ibm.ws.admin.core.jar`

Place these jar files in `remora-websphere/lib` folder before running a build.

# Remora Java Agent 
## Using -javaagent option

### IBM WebSphere

* Option 1: using IBM WebSphere console.

    * Step 1:    Navigate to `Application servers > [Your server name] > Process definition > Java Virtual Machine`
    * Step 2:    Edit field "Generic JVM arguments"
    * Step 3:    Add `-javaagent:[<install_dir>/remora-<version>]/remora.jar`
    * Step 4:    Restart IBM WebSphere 
    * Step 5:    Run and configure Streams forwarding agent

* Option 2: editing `server.xml` properties manually

    * Step 1:    Navigate to `<USER_DIR>/IBM/WebSphere/<Server>/profiles/<App Server>/config/cells/<Cell>/nodes/<Node>/servers/<Server>/server.xml`
    * Step 2:    Edit node `/process:Server/processDefinitions/jvmEntries` parameter `@genericJvmArguments`
    * Step 3:    Edit the path to where your `remora.jar` situated
    ```xml
    <jvmEntries xmi:id="JavaVirtualMachine_1183122130078" verboseModeClass="false" verboseModeGarbageCollection="false" verboseModeJNI="false" initialHeapSize="512" maximumHeapSize="2056" runHProf="false" hprofArguments="" genericJvmArguments="-javaagent:<install_dir>/remora-<version>/remora.jar" executableJarFileName="" disableJIT="false">
    ```

### IBM WAS Liberty

* Step 1:    Edit or create `jvm.options` file in the folder `wlp/usr/servers/<serverName>/`
* Step 2:    Add lines:
```
-javaagent:<install_dir>/remora-<version>/remora.jar -Dremora.path
```
* Step 3:    Edit the path to where your `remora.jar` situated
* Step 4:    Edit or create `bootstrap.properties` in the folder `wlp/usr/servers/<serverName>/`
* Step 5:    Add line:
```
org.osgi.framework.bootdelegation=com.jkoolcloud.remora.*
```

### JBoss Application Server

#### Standalone mode

* Step 1:    Edit `bin/standalone.bat[.sh]`
* Step 2:    Add line: 
```
    winx: set "JAVA_OPTS=%JAVA_OPTS% -javaagent:<install_dir>/remora-<version>/remora.jar"
    unix: JAVA_OPTS="$JAVA_OPTS -javaagent:<install_dir>/remora-<version>/remora.jar"
```
* Step 3:    Edit the path to where your `remora.jar` situated

#### Domain mode

* Step 1:    Edit `domain/configuration/host.xml`
* Step 2:    Edit tag `<servers><jvm>`
* Step 3:    Add `<option value="-javaagent:<install_dir>/remora-<version>/remora.jar"/>`
```xml
       <jvm name="default">
           <jvm-options>
           </jvm-options>
               <option value="-agentlib:jdwp=transport=dt_socket,address=5007,server=y,suspend=n"/>
               <option value="-javaagent:<install_dir>/remora-<version>/remora.jar"/>
       </jvm>
```
* Step 4:    Edit the path to where your `remora.jar` situated

### Standalone Java Application

To run you standalone application with RemoraJ add option `-javaagent:<install_dir>/remora-<version>/remora.jar` to your run script or command line:

```
java -javaagent:<install_dir>/remora-<version>/remora.jar -jar <jar-file-name>.jar
```

## Configure Streams Agent

* Step 1:    Go to `<install_dir>/remora-<version>/tnt4j-streams/config`
* Step 2:    Edit `tnt4j-streams.properties` and setup your access token (`event.sink.factory.EventSinkFactory.prod.Token`)
* Step 3:    (Optional) More settings in `<install_dir>/remora-<version>/tnt4j-streams/remora-streamer/tnt-data-source.xml` 
* Step 4:    (Optional) Edit ```<property name="FileName" value="../../queue"/>``` to point to your RemoraJ queue directory.

**NOTE:** RemoraJ queue directory hosts files containing java traces produced by remora java agent. These traces are read by the Streams Agent and forwarded to your data repository. 

# Remora Agent Configuration

RemoraJ configuration file is located `config` folder, file named `remora.properties`.
See relevant comments in the file for advanced config.

## Agent VM Id

To identify your VM: set system property (`java -Dremora.vmid=MyVmName`) to any readable token identifying the process remora agent is 
attached to. `remora.vmid` is set to `ManagementFactory.getRuntimeMXBean().getName()` by default.

**NOTE:** `remora.vmid` value might be not available on every VM's implementation and may be platform dependent.

## Application Id
 
To identify your application: set system property (`java -Dremora.appl.name=MyApp1`). All emmited traces are tagged with the specified app name. Application name is automatically set based on deployed app name when remora agent is running in the application server context such as jBoss. 

**NOTE:** Default `remora.appl.name` is set to `java` if not explicitly specified.

## Agent Trace Output

By default, RemoraJ java agent writes its traces to a high-performance memmory mapped persistent store (file) backed by `com.jkoolcloud.remora.core.output.ChronicleOutput`. This store is used as a communication channel between RemoraJ java agent and the Streams Agent.

There are several trace outputs available below:

* `com.jkoolcloud.remora.core.output.NullOutput` -- no output (null output)
* `com.jkoolcloud.remora.core.output.SysOutOutput` -- `System.out` (console)
* `com.jkoolcloud.remora.core.output.ChronicleOutput` -- persistent memory mapped file-based output **(default)**

File system queue is created only with `com.jkoolcloud.remora.core.output.ChronicleOutput`. Set system property (`java -Dremora.output`) with the trace output class reference (e.g. `-Dremora.output=com.jkoolcloud.remora.core.output.SysOutOutput`).

## Advice Trace Filters
Advices can have trace filters. Filters determine which trace are include vs. excluded. You configure the filter by defining one in `remora.properties`.
```properties
<prefix for filter definition - filter>.<unique filter name>.<filter property>
```
for example:
```properties
filter.myDefinedFilter.type=com.jkoolcloud.remora.filters.ClassFilter
filter.myDefinedFilter.mode=INCLUDE/EXCLUDE
filter.myDefinedFilter.classes=java.net.SocketInputStream
```
to apply the filter(s) to advice, add as `advice` property:

```
com.jkoolcloud.remora.advices.<advice name>.filters=<unique filter name>;<unique filter name2>;<unique filter name3>
```

# Running Streams Agent

Once the Remora Java Agent is running and the Streams Agent configured you can run `<install_dir>/remora-<version>/tnt4j-streams/remora-streamer/run.sh(run.bat)` to start the Streams Agent and begin forwarding java traces to your trace output.

# Troubleshooting and Logging

If there is some problems running you can always check the logs. Logging might cause seriuos overhead, so by default the logging option is turned off.
In order to turn on the logging you need to change configuration file, or you can turn it on remotelly if you are using `remora-control` module.

To turn on logging you need to change option `com.jkoolcloud.remora.advices.BaseTransformers.logging` to `true` in the file `remora.properties`. You can also turn on logging for individual advices.

Each advice creates it own logging file. You can find all files in your remora's folder under `log` -- `<install_dir>/remora-<version>/log`.

# Supported Analytics Platforms
* [jKool](https://www.jkoolcloud.com/) -- SaaS platform for analyzing high velocity machine data
* [Nastel XRay](https://xray.nastel.com/xray/Nastel/login.jsp) -- AIOps & transaction tracking. Supports SaaS/On-prem/Cloud
* Integrate traces into ELK, Splunk or other platform via [agent output class implementation](#agent-trace-output)

