FROM openjdk:latest

#Get the build jar file in target
ARG JAR_FILE=target/*.jar

#Copy the jar file
COPY ${JAR_FILE} management-0.0.1-SNAPSHOT.jar

#Run the java application
ENTRYPOINT ["java","-jar","/management-0.0.1-SNAPSHOT.jar"]