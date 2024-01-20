# <div align="center">Medilabo Solutions![Alt text](image-1.png)</div>

<p style="text-align:center;">A Spring-boot demo application to help doctors to identify patients most at risk of type 2 diabetes.</p>

  <div style="text-align:center;">

  ![Java](https://img.shields.io/badge/17-%23437291?style=plastic&logo=Openjdk&logoColor=%23437291&label=OpenJdk&labelColor=grey) ![SpringBoot](https://img.shields.io/badge/3.1.7-grey?style=plastic&logo=Spring-Boot&logoColor=green&label=Spring-Boot&labelColor=grey&color=green) ![Spring Security](https://img.shields.io/badge/6.0.6-grey?style=plastic&logo=Spring-Security&logoColor=green&label=Spring-Security&labelColor=grey&color=green) ![Spring Webflux](https://img.shields.io/badge/6.0.15-grey?style=plastic&logo=react&logoColor=green&label=Spring_Webflux&labelColor=grey&color=green) </br> ![Thymeleaf](https://img.shields.io/badge/3.1.7-%23005F0F?style=plastic&logo=thymeleaf&logoColor=%23005F0F&label=Thymeleaf&labelColor=grey) ![MySql](https://img.shields.io/badge/8.2.0-%234479A1?style=plastic&logo=mysql&logoColor=%234479A1&label=MySql&labelColor=grey) ![MongoDB](https://img.shields.io/badge/6.0.13-%2347A248?style=plastic&logo=mongodb&logoColor=%2347A248&label=MongoDB&labelColor=grey) ![Docker](https://img.shields.io/badge/24.0.7-%232496ED?style=plastic&logo=docker&logoColor=%232496ED&label=Docker&labelColor=grey)

</div>

---

- **Table of content**
  - [Architecture](#architecture)
  - [Security](#Security)
  - [Tests](#Tests)
  - [Run application](#run-application)
    - [With docker](#with-docker)
    - [Locally](#locally)
    - [Credential](#credential)
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

## Security

We use Spring security to securize application.

For this moment, as application is not a release version, we have just implented one registred user in memory (see chapter of [credential](#credential)) to log in it.

The secure access to application follows the following principles:

- First a classic authentication username-password with **credential in the web-app** through form in login page
- Next a **Httpbasic authentication** to access to microservice through Gateway-service
- Passing by a authentication and authorization delivered by Auth-server microservice

---

## Run application

### With docker

We suppose that you have already install and configure Docker <img src="docker-color.svg" alt="docker-svg" width="30"> and it's compose plugin on your computer. If it's not the case, you can follow these documentations:

- [Install Docker](https://docs.docker.com/get-docker/)
- [Install Docker Compose](https://docs.docker.com/compose/install/)

After a completed installation, as all microservices have their own ***DockerFile*** and there is a ***docker-compose.yaml*** at the root of project. You can run the project just by using the following command without worrying about the compilation order and dependencies:

```bash
docker compose up -d
```

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

### Credential

We use Spring Security to secure application. As the application is not yet a release but a testing version, a predefined user was registred in Memory. So, you must use his basic credential, defined below, to login to application:

```bash
username:   user
password:   password
```

---

## Tech Stacks

- **Java 17**
- **Maven 3.1.2**
- **Spring-Boot 3.1.7**
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
