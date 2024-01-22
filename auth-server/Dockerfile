FROM openjdk:17-alpine
RUN apk add --no-cache curl
LABEL application-name="auth-server"
LABEL maintainer="delaval.htps@gmail.com"
LABEL version="V0.0.2"
VOLUME [ "/main-app" ]
ADD target/auth-server-0.0.2.jar auth-server.jar
EXPOSE 8084
ENTRYPOINT [ "java","-jar","/auth-server.jar" ]