FROM openjdk:17-alpine
LABEL application-name="web-app"
LABEL maintainer="delaval.htps@gmail.com"
LABEL version="V0.0.2"
VOLUME [ "/main-app" ]
ADD target/web-app-0.0.2.jar web-app.jar
EXPOSE 8080
ENTRYPOINT [ "java","-jar","/web-app.jar" ]