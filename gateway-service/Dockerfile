FROM openjdk:17-alpine
RUN apk add --no-cache curl
LABEL application-name="gateway-service"
LABEL maintainer="delaval.htps@gmail.com"
LABEL version="V0.0.2"
VOLUME [ "/main-app" ]
ADD target/gateway-service-0.0.2.jar gateway-service.jar
EXPOSE 8081
ENTRYPOINT [ "java","-jar","/gateway-service.jar" ]