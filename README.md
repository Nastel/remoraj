
# RemoraJ

RemoraJ is a java application monitoring tool. RemoraJ monitors your application performance by tracking common application building blocks and services services like HTTP, JMS, JDBC requests. RemoraJ consists of tw0 parts: java agent running within application's JVM and tnt4j-streams which is an external agent that fordwards java traces to an central analytics platforms such as https://www.jkoolcloud.com/. These traces are send using JSON over HTTPS.

# Installing

You must set up Remora Java agent and TNT4J-Streams to run RemoraJ. Your should have working repository and token in https://www.jkoolcloud.com/.  

## Java agent 
## Using -javaagent option

### IBM WebSphere

Option 1: using IBM WebSphere console.
 
* Step 1    Navigate to Application servers > [Your server name] > Process definition > Java Virtual Machine
* Step 2    Edit field "Generic JVM arguments"
* Step 3    Add -javaagent:[c:\remora]\remora.jar=[c:\remora]
* Step 4    Restart IBM WebSphere 
* Step 5    Run and configure TNT4J streams forwarding agent

Option 2: editing server.xml properties manually 

* Step 1    Navigate to `c:\Users\<USER_DIR>\IBM\WebSphere\<Server>\profiles\<App Server>\config\cells\<Cell>\nodes\<Node>\servers\<Server>\server.xml`
* Step 2    Edit node `/process:Server/processDefinitions/jvmEntries` paramter `@genericJvmArguments`
* Step 3    Edit the path to where your remora.jar situated
```
<jvmEntries xmi:id="JavaVirtualMachine_1183122130078" verboseModeClass="false" verboseModeGarbageCollection="false" verboseModeJNI="false" initialHeapSize="512" maximumHeapSize="2056" runHProf="false" hprofArguments="" genericJvmArguments="-javaagent:c:\remora\remora-0.1.1-SNAPSHOT\remora.jar=c:\remora\remora-0.1.1-SNAPSHOT\" executableJarFileName="" disableJIT="false">

```

### IBM WAS Liberty 


* Step 1	Edit or create `jvm.options` file in the folder [wlp\usr\servers\<serverName>\].  
* Step 2	Add lines:
```
-javaagent:c:\workspace\build\remora\remora-0.1.1-SNAPSHOT\remora.jar
-Dremora.path=c:\workspace\build\remora\remora-0.1.1-SNAPSHOT
```

* Step 3	Edit the path to where your remora.jar situated
* Step 4	Edit or create `bootstrap.properties` in the folder [wlp\usr\servers\<serverName>\].
* Step 5	Add line:
```
org.osgi.framework.bootdelegation=com.jkoolcloud.remora.*
```




### JBoss application server

#### Standalone mode

* Step 1    Edit `bin\standalone.bat`
* Step 2    Add line 
```set "JAVA_OPTS=%JAVA_OPTS% -javaagent:c:\remora\remora-0.1.1-SNAPSHOT\remora.jar=c:\remora\remora-0.1.1-SNAPSHOT\"```
* Step 3    Edit the path to where your remora.jar situated

#### Domain mode

* Step 1    Edit `domain\configuration\host.xml`
* Step 2    Edit tag `<servers><jvm>`
* Step 3    Add `<option value="-javaagent:c:\remora\remora-0.1.1-SNAPSHOT\remora.jar=c:\remora\remora-0.1.1-SNAPSHOT\"/>`  
```            <jvm name="default">
                   <jvm-options>
                       <option value="-agentlib:jdwp=transport=dt_socket,address=5007,server=y,suspend=n"/>
                       <option value="-javaagent:c:\remora\remora-0.1.1-SNAPSHOT\remora.jar=c:\remora\remora-0.1.1-SNAPSHOT\"/>
                   </jvm-options>
               </jvm>
```
* Step 3    Edit the path to where your remora.jar situated

## Configure TNT4J streams

* Step 1    Go to tnt4j streams config
* Step 2    Edit `tnt4j-streams.properties` and setup your access Token (`event.sink.factory.EventSinkFactory.prod.Token`)
* Step 3    (Optional) Edit tnt `remora-0.1.1-SNAPSHOT\tnt4j-streams\remora-streamer\tnt-data-source.xml` 
* Step 4    (Optional) Setup line ```<property name="FileName" value="..\..\queue"/>``` to point to your RemoraJ queue directory.


# Configuration

RemoraJ configuration file is located `config` folder, file named `remora.properties`.
See relevant comments in file for advanced config.

# Running

Once the agent atached and TNT4J-Streams configured you can 
Run `remoraJ\tnt4j-streams\remora-streamer\run.sh(run.bat)`.
  


