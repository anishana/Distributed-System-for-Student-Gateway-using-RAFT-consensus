FROM openjdk:latest
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} management-0.0.1-SNAPSHOT.jar
ENTRYPOINT ["java","-jar","/management-0.0.1-SNAPSHOT.jar"]