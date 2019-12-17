# Intro

This folder contains sample applications one could use to demonstrate how Remora Java agent works.

* PlantsByWebsphere - sample application shipped with IBM application server products (WAS and Liberty). 
						The application demonstrates several Java Platform, Enterprise Edition (Java EE) 
						functions, using an online store that specializes in plant and garden tool sale.
						
* Bank				- Nastel sample bank application. The application demonstrates several Java Platform, 
						Enterprise Edition (Java EE), JMS and other features.
						

PlantsByWebsphere application uses Bank on checkout and reduces the bank balance on completed order using REST call




## Building and running PlantsByWebsphere 

### Prerequisites

* Gradle - https://downloads.gradle-dn.com/distributions/gradle-5.2.1-bin.zip


### Installing PlantsByWebsphere


* Step 1:	select database to use (Derby or mySQL) skip to [step 7] if you choose Derby 
* Step 2:	install and run mySQL server
* Step 3:	import SQL script located:  `plantsbywebsphere\sqlScripts\`
* Step 4:	build the application "gradle start" - this step could fail we do only need to get the server installed
* Step 4:	stop application "gradle stop"
* Step 5:	copy mySQL driver into `plantsbywebsphere\build\wlp\usr\shared\resources\mysql`
* Step 6:	continue to [step 8]

* Step 7:	modify server.xml, uncomment Derby data source configuration

* Step 8:	modify `plantsbywebsphere\src\main\liberty\config\jvm.options`, change remoraJ paths here.
* Step 9:	`gradle start open` should open Internet browser with application running


## Installing Bank
 
* Step 1:	deploy database from folder`samples\bank\sql`
* Step 2:	configure JBoss, change these files accordingly your system configuration and deploy all files in folder 	`\bank\config\jboss\jboss7.2\`, wmq.jmsra-9.0.4.0.rar - will install IBM MQ JMS driver, mysql-ds7.xml - will configure database, jms-ds7.xml - will configure JMS. Attach java agent (see readme on RemoraJ)
* Step 3: 	Create `BankRequestQueue` and `BankReplyQueue`
* Step 4:   mvn clean install wildfly:deploy
* Step 5:	open bank application http://localhost:8080/Bank-1.0-SNAPSHOT/









 
