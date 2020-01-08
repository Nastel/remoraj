# Intro

This folder contains sample applications one could use to demonstrate how Remora Java agent works.

* PlantsByWebsphere - sample application shipped with IBM application server products (WAS and Liberty). 
    The application demonstrates several Java Platform, Enterprise Edition (Java EE) functions, using an online store that specializes in 
    plant and garden tool sale.

* Bank              - Nastel sample bank application. The application demonstrates several Java Platform, Enterprise Edition (Java EE), 
    JMS and other features.

PlantsByWebsphere application uses Bank on checkout and reduces the bank balance on completed order using REST call

## Building and running PlantsByWebsphere 

### Prerequisites

* Gradle - https://downloads.gradle-dn.com/distributions/gradle-5.2.1-bin.zip
* MySQL 5.7
* IBM MQ 8.5


### Installing PlantsByWebsphere

* Step 1:	download and extract gradle to 'c:\gradle\' . Modify Windows path: `System properties -> Advanced -> Enviroment variables -> path` add 'c:\gradle\bin\'
* Step 2:	install and run mySQL server
* Step 3:	import SQL script located:  `plantsbywebsphere\sqlScripts\`
* Step 4:	modify `plantsbywebsphere\src\main\liberty\config\jvm.options`, change remoraJ paths here.
* Step 5:	`gradle start open` should open Internet browser with application running


## Installing Bank JBoss

* Step 1:    deploy database from folder`samples\bank\sql`
* Step 2:    configure JBoss, change these files accordingly your system configuration and deploy all files in folder 
`\bank\config\jboss\jboss7.2\`, wmq.jmsra-9.0.4.0.rar - will install IBM MQ JMS driver, mysql-ds7.xml - will configure database, 
jms-ds7.xml - will configure JMS. Attach java agent (see readme on RemoraJ)
* Step 3:    run the JBoss with all modules loaded "standalone.bat -c standalone-full.xml" 
* Step 4:    create `BankRequestQueue` and `BankReplyQueue`
* Step 5:    run `mvn clean install wildfly:deploy`
* Step 6:    open bank application `http://localhost:8080/Bank-1.0-SNAPSHOT/`


## Installing Bank Apache Tomcat 9.0.x

* Step 1:	copy mysql driver (mysql-connector-java-8.0.18.jar) to tomcat/lib folder
* Step 2:	copy IBM JMS lib and javax.jms-api-2.0.1.jar to tomcat/lib folder
* Step 3:	deploy war, copy  Bank*.war to tomcat/webapps
* Step 4:	configure JDBC and JMS, add lines to config/context.xml
```  
	<Resource name="bank_db" auth="Container" type="javax.sql.DataSource"
               maxTotal="100" maxIdle="30" maxWaitMillis="10000"
               username="root" password="slabs123" driverClassName="com.mysql.jdbc.Driver"
               url="jdbc:mysql://localhost:3306/bank?autoReconnect=true"/>
	<Resource
      name="BankRequestQueue"
      auth="Container"
      type="com.ibm.mq.jms.MQQueue"
      factory="com.ibm.mq.jms.MQQueueFactory"
      description="JMS Queue for receiving messages from Dialog"
      QU="BankRequestQueue"/>
   <Resource
      name="BankReplyQueue"
      auth="Container"
      type="com.ibm.mq.jms.MQQueue"
      factory="com.ibm.mq.jms.MQQueueFactory"
      description="JMS Queue for receiving messages from Dialog"
      QU="BankReplyQueue"/>
			   
````
* Step 5:	configure IBM MQ
* Step 6:	open bank application http://localhost:8080/Bank-1.0-SNAPSHOT/









 
