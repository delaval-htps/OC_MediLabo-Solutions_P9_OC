FROM openjdk:17-alpine
LABEL application-name="note-service"
LABEL maintainer="delaval.htps@gmail.com"
LABEL version="V0.0.1-SNAPSHOT"
VOLUME [ "/main-app" ]
ADD target/note-service-0.0.1-SNAPSHOT.jar note-service.jar
EXPOSE 8085
ENTRYPOINT [ "java","-jar","/note-service.jar" ]