FROM openjdk:17-alpine
LABEL application-name="gateway-server"
LABEL maintainer="delaval.htps@gmail.com"
LABEL version="V0.0.1-SNAPSHOT"
VOLUME [ "/main-app" ]
ADD target/gateway-service-0.0.1-SNAPSHOT.jar gateway-service.jar
EXPOSE 8081
ENTRYPOINT [ "java","-jar","/gateway-service.jar" ]