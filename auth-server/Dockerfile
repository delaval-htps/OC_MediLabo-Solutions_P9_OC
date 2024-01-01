FROM openjdk:17-alpine
LABEL application-name="auth-server"
LABEL maintainer="delaval.htps@gmail.com"
LABEL version="V0.0.1-SNAPSHOT"
VOLUME [ "/main-app" ]
ADD target/auth-server-0.0.1-SNAPSHOT.jar auth-server.jar
EXPOSE 8084
ENTRYPOINT [ "java","-jar","/auth-server.jar" ]