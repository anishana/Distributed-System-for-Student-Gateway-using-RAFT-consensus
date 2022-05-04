FROM openjdk:latest

#Get the build jar file in target
ARG JAR_FILE=target/*.jar

#Copy the jar file
COPY ${JAR_FILE} Controller_Java-0.0.1-SNAPSHOT.jar

#Run the java application
ENTRYPOINT ["java","-jar","/Controller_Java-0.0.1-SNAPSHOT.jar"]