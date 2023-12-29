FROM openjdk:17-alpine
LABEL application-name="config-server"
LABEL maintainer="delaval.htps@gmail.com"
LABEL version="V0.0.1-SNAPSHOT"
VOLUME [ "/main-app" ]
ADD target/config-server-0.0.1-SNAPSHOT.jar config-server.jar
EXPOSE 9101
ENTRYPOINT [ "java","-jar","/config-server.jar" ]