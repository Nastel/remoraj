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


### Installing


* Step 1:	select database to use (Derby or mySQL) skip to [step 7] if you choose Derby 
* Step 2:	install and run mySQL server
* Step 3:	import SQL script located:  `plantsbywebsphere\sqlScripts\`
* Step 4:	build the application "gradle start" - this step could fail we do only need to get the server installed
* Step 4:	stop application "gradle stop"
* Step 5:	copy mySQL driver into `plantsbywebsphere\build\wlp\usr\shared\resources\mysql`
* Step 6:	continue to [step 8]

* Step 7:	modify server.xml [TODO ]

* Step 8:	`gradle start open` should open internet browser with application running


## Building Bank
 
* Step 1:	deploy database, configure jdbc, JMS [TODO ]
* Step 2:   mvn clean install wildfly:deploy









 