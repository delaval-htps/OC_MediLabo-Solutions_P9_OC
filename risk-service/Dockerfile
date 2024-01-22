FROM openjdk:17-alpine
RUN apk add --no-cache curl
LABEL application-name="risk-service"
LABEL maintainer="delaval.htps@gmail.com"
LABEL version="V0.0.2"
VOLUME [ "/main-app" ]
ADD target/risk-service-0.0.2.jar risk-service.jar
EXPOSE 8086
ENTRYPOINT [ "java","-jar","/risk-service.jar" ]