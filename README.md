# <div align="center">Medilabo Solutions![Alt text](image-1.png)</div>

<p style="text-align: center;">A Spring-boot application to help doctors to identify patients most at risk of type 2 diabetes.</p>

  <div style="height:30px;text-align:center;">

  ![Spring](https://img.shields.io/badge/spring?style=plastic&logo=spring) ![Alt text](image-2.png){: height="30px"} ![Java](https://img.shields.io/badge/java?style=plastic&logo=openjdk) ![Thymeleaf](https://img.shields.io/badge/Thymeleaf?style=plastic&logo=Thymeleaf) ![MySQL](https://img.shields.io/badge/mysql?style=plastic&logo=mysql) ![MongoDB](https://img.shields.io/badge/MongoDB?style=plastic&logo=mongodb) ![Docker](https://img.shields.io/badge/docker?style=plastic&logo=docker)

</div>



---

- **Table of content**
  - [Architecture](#architecture)
  - [Run application](#run-application)
    - [Locally](#locally)
    - [With docker](#with-docker)
  - [Tech Stacks](#tech-stacks)
  - [Improvements](#improvements)
  - [Versions](#versions)
  
---

## Architecture

![Alt text](Architecture.drawio.png)

As we can see in architectural diagram above,this Spring-boot application is a multi modules project. Each module is a micro-service and has one task with one dedicated port:

- **On BackEnd:**

Name  | Port| Definition
------|------|---------
 **Gateway-service** | 8081| route requests from web-app to Rest API micro-services
 **Eureka-server**   | 8082| manage a registry of all micro-service's instances using load balancing
 **Config-server**   | 9101| server to deliver external configuration properties for each microservices
 **Auth-server**     |8084| Rest API to check authentication and authorization of user connected to web-app
 **Patient-service** |8083 | Rest API connected to MySql dbto manage registred patients in the application
 **Note-service** |8085|Rest API connected to MongoDb db to manage registred notes of patients provided by doctors
 **Risk-service** |8086|Rest API to provide disease risk assessment for a patient base on his notes

- **On Front-end:**

Finally, we have a web application , it could be a mobile application or Single Page Application, but for this project, we use a web application with Spring-boot and Thymeleaf to do the job. Of course, it's place is between the gateway and the user's browser and provides a IHM with html pages.

Name         | Port | Definition
-------------|------|----------
 **web-app** | 8080 | provide a GUI for browser, requests to Gateway and create html pages with results ...

---

## Run application

### Locally

1. You have to create manually patient-service database in your Mysql db
2. You have to
3. Use the maven command `mvn spring-boot:run` to start all microservices in this application. But don't forget to respect the following order

    Order | name of service
    ------|----------------
    1|Config-server
    2|Eureka-server
    3|Gateway-service
    4|Auth-server
    5|Patient-service
    6|Note-service
    7|Risk-service
    8|web-app

### With docker

All microservices have a ***DockerFile*** and we created a ***docker-compose.yaml*** at the root of project.
So, you can run the project just by using the following command without worrying about the compilation order:

```bash
docker compose up -d
```

---

## Tech Stacks

- **Java 17**
- **Maven 3.1.2**
- **Spring-webflux 6.0.15**
- **Spring-security 6.1.6**
- **Spring-cloud-config 4.0.4**
- **Spring-cloud-gateway 4.0.7**
- **Spring-cloud-netflix-eureaka 4.0.3**
- **Spring-boot actuator 3.1.17**
- **Mongodb reactive 3.1.7**
- **R2dbc-Mysql 1.0.2**
- **Thymeleaf 3.1.7**
- **Jupiter 5.9.3**
- **Jacoco 0.8.10**
- **Lombok 1.18.30**

---

## Improvements

---

## Versions
