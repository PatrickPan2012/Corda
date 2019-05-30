# My Environments
  - JDK 1.8.0_191
  - Tomcat 9.0.14

# Installation
  - Change log path in "src/main/resources/logback.xml"
  - Execute cmd "./gradlew clean build" to build and copy "build/libs/corda-doorman.war" to Tomcat
  - Start up Tomcat
  
# Demo
  - Unzip "corda-release-V3-doorman.zip"
  - Change "networkMapURL" in "corda-release-V3-doorman/node.conf" according to Tomcat port while leaving alone "networkMapURL"
  - Change working directory to "corda-release-V3-doorman" 
  - Execute cmd "java -jar corda.jar --initial-registration --network-root-truststore-password trustpass"
  - Many files will be created automatically. Especially, "nodekeystore.jks", "sslkeystore.jks" and "truststore.jks" can be found in  "certificate" folder. Use some tools such as "KeyStore Explorer" to open "sslkeystore.jks" with password "cordacadevpass" and view "cordaclienttls", legal name specified in "NodeCertificateManager.createDoormanCertificateAndKeyPair" can be seen in the certificate chain.

More details can be seen [here](https://blog.csdn.net/ItachiUchiha/article/details/90705573).