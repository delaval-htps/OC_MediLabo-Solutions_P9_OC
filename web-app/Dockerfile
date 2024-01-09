FROM openjdk:17-alpine
LABEL application-name="web-app"
LABEL maintainer="delaval.htps@gmail.com"
LABEL version="V0.0.1-SNAPSHOT"
VOLUME [ "/main-app" ]
ADD target/web-app-0.0.1-SNAPSHOT.jar web-app.jar
EXPOSE 8080
ENTRYPOINT [ "java","-jar","/web-app.jar" ]