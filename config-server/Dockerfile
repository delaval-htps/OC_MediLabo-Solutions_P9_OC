FROM openjdk:17-alpine
RUN apk add --no-cache curl
LABEL application-name="config-server"
LABEL maintainer="delaval.htps@gmail.com"
LABEL version="V0.0.2"
VOLUME [ "/main-app" ]
ADD target/config-server-0.0.2.jar config-server.jar
EXPOSE 9101
ENTRYPOINT [ "java","-jar","/config-server.jar" ]