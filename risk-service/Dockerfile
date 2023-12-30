FROM openjdk:17-alpine
LABEL application-name="risk-service"
LABEL maintainer="delaval.htps@gmail.com"
LABEL version="V0.0.1-SNAPSHOT"
VOLUME [ "/main-app" ]
ADD target/risk-service-0.0.1-SNAPSHOT.jar risk-service.jar
EXPOSE 8085
ENTRYPOINT [ "java","-jar","/risk-service.jar" ]