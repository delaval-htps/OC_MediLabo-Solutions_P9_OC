FROM openjdk:17-alpine
RUN apk add --no-cache curl
LABEL application-name="note-service"
LABEL maintainer="delaval.htps@gmail.com"
LABEL version="V0.0.2"
VOLUME [ "/main-app" ]
ADD target/note-service-0.0.2.jar note-service.jar
EXPOSE 8085
ENTRYPOINT [ "java","-jar","/note-service.jar" ]