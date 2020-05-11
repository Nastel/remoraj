
# RemoraJ: Light-Weight Java ByteCode Agent for App Performance

RemoraJ is a light weight java app profiling agent which uses bytecode instrumentation to profile java application performance, exceptions with minimal overhead. RemoraJ monitors java apps by tracking method calls and activities such as: HTTP, WebServices, JMS, JDBC, WebSockets, Kafka and more. 

RemoraJ consists of two main runtime components:

* `Remora Java Agent`: loaded into your JVM, app server, instruments your code, emits traces and metrics
* `Remora Streams Agent`: forwards the traces generated by the java agent to one of the supported analytics platforms below

**Supported Analytics Platforms**
* [jKool](https://www.jkoolcloud.com/) -- SaaS platform for analyzing high velocity machine data
* [Nastel XRay](https://www.nastel.com/nastel-xray/) -- Platform for AIOps supports SaaS/On-prem/Cloud

Both agents communicate via a memory mapped queue stored on a filesystem see `tnt-data-source.xml` for details. The Streams Agent sends traces using JSON over HTTPS.

**NOTE:** The Streams Agent project is located here: https://github.com/Nastel/tnt4j-streams

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

* Obtain your account with [jKool](https://www.jkoolcloud.com/) or [Nastel XRay](https://www.nastel.com/nastel-xray/). Make sure to get your streaming access token
* Configure the Remora Java Agent for your JVM (captures java call traces, metrics)
* Configure the Streams Agent (you will need your streaming access token here)
* Start your application or app server (with remora agent)
* Start the Streams Agent (forwards traces to your data repository associated with your streaming access token)

Login to your dashboard to view & analyze results: 
* [jKool Dashboard](https://jkool.jkoolcloud.com/jKool/login.jsp)
* [Nastel XRay Dashboard](https://xray.nastel.com/xray/Nastel/login.jsp)

## Prerequisites

Java >8 runtime (IBM WebSphere >8.5.5, Tomcat >8, Jboss >7.2 should be already running Java 8. Make sure JVM is Java 1.8 or above).

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

## Agent VM Identification

To identify your VM: can set system property (`java -D`) `remora.vmid` to any readable token identifying the process remoraJ are 
attached to. By default it will use `ManagementFactory.getRuntimeMXBean().getName()` result (e.g. `-Dremora.vmid=myVM1001`).

**NOTE:** Default `remora.vmid` value might be not available on every VM's implementation and may be platform dependent.

## Agent Trace Output

By default, RemoraJ java agent writes its traces to a high-performance memmory mapped persistent store (file) backed by `com.jkoolcloud.remora.core.output.ChronicleOutput`. This store is used as a communication channel between RemoraJ java agent and the Streams Agent.

There are several trace outputs available below:

* `com.jkoolcloud.remora.core.output.NullOutput` -- no output (null output)
* `com.jkoolcloud.remora.core.output.SysOutOutput` -- `System.out` (console)
* `com.jkoolcloud.remora.core.output.ChronicleOutput` -- persistent memory mapped file-based output **(default)**

No file system queue will be created, when one of the above outputs are configured. 
To select output set system property (`java -D`) `remora.output` with full class reference (e.g. `-Dremora.output=com.jkoolcloud.remora.core.output.SysOutOutput`)

# Running Streams Agent

Once the Remora Java Agent is running and Streams Agent configured you can 
Run `<install_dir>/remora-<version>/tnt4j-streams/remora-streamer/run.sh(run.bat)` to start Streams Agent and begin forwarding java traces to your data repository.

# Troubleshooting and Logging

If there is some problems running you can always check the logs. Logging might cause seriuos overhead, so by default the logging option is turned off.
In order to turn on the logging you need to change configuration file, or you can turn it on remotelly if you are using `remora-control` module.

To turn on logging you need to change option `com.jkoolcloud.remora.advices.BaseTransformers.logging` to `true`in the file `remora.properties`. You can also turn on logging for individual advices.

Each advice creates it own logging file. You can find all files in your remora's folder under `log` -- `<install_dir>/remora-<version>/log`.

# Supported Analytics Platforms
* [jKool](https://www.jkoolcloud.com/) -- SaaS platform for analyzing high velocity machine data
* [Nastel XRay](https://www.nastel.com/nastel-xray/) -- Platform for AIOps supports SaaS/On-prem/Cloud
