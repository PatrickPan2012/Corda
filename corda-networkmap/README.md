# My Environments
  - JDK 1.8.0_191
  - Tomcat 9.0.17

# Installation
  - Change log path in "src/main/resources/logback.xml".
  - Execute cmd "./gradlew clean build" to build and copy "build/libs/corda-networkmap.war" to Tomcat.
  - Start up Tomcat.
  
# Demo
  - Suppose "Corda Doorman" is running otherwise please set it up first.
  - Unzip "corda-release-V3-networkmap.zip".
  - Respectively change working directory to "Notary"/"PartyA"/"PartyB" and modify URL in "node.conf" if necessary before executing cmd "java -jar corda.jar --initial-registration --network-root-truststore-password keystorepass".
  - Copy "Notary/certificates/nodekeystore.jks" to "TOMCAT_HOME/webapps/corda-networkmap/WEB-INF/classes/notaries".
  - Copy "PartyA/cordapps/cordapp-example-0.1.jar" to "TOMCAT_HOME/webapps/corda-networkmap/WEB-INF/classes/cordapps".
  - Respectively change working directory to "Notary"/"PartyA"/"PartyB" and execute cmd "java -jar corda.jar".
  - Respectively change working directory to "PartyA"/"PartyB" and execute cmd "java -jar corda-webserver.jar".
  - Use Chrome or other browsers to visit "http://localhost:10009/web/example/" and "http://localhost:10012/web/example/" and create "IOU".