# AutoConfig

Full Stack system designed to manage vehicle accessory configuration workflow inside car dealerships, replacing informal WhatsApp communication with a structured, traceable, and scalable solution.

---

![Java](https://img.shields.io/badge/Java-17-red?logo=openjdk\&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-brightgreen?logo=springboot)
![Spring Security](https://img.shields.io/badge/Spring_Security-JWT-6DB33F?logo=springsecurity\&logoColor=white)
![JWT](https://img.shields.io/badge/Auth-JWT-blue)
![JPA](https://img.shields.io/badge/JPA-Hibernate-orange)
![Flyway](https://img.shields.io/badge/Flyway-Migrations-CC0200?logo=flyway\&logoColor=white)
![H2](https://img.shields.io/badge/H2-DevDB-09476B)
![MySQL](https://img.shields.io/badge/MySQL-ProdDB-4479A1?logo=mysql\&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-Build-C71A36?logo=apachemaven\&logoColor=white)
![React](https://img.shields.io/badge/React-Frontend-61DAFB?logo=react\&logoColor=black)
![TypeScript](https://img.shields.io/badge/TypeScript-Language-3178C6?logo=typescript\&logoColor=white)
![Vite](https://img.shields.io/badge/Vite-Build-646CFF?logo=vite\&logoColor=white)
![ESLint](https://img.shields.io/badge/ESLint-CodeQuality-4B32C3?logo=eslint\&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker\&logoColor=white)
![GitHub Actions](https://img.shields.io/badge/GitHub_Actions-CI-2088FF?logo=githubactions\&logoColor=white)
![Railway](https://img.shields.io/badge/Railway-Cloud-0B0D0E?logo=railway\&logoColor=white)

---

## Overview

AutoConfig is a web application created to organize and standardize the process of selling and installing accessories on new vehicles inside car dealerships.

Currently, many dealerships rely on WhatsApp groups to communicate information between salespeople, stock teams, and installation teams. This approach creates disorganization, outdated information, and communication failures.

AutoConfig centralizes this workflow into a single system, ensuring data consistency, traceability, and efficiency across all departments involved in vehicle preparation.

---

## Problem

When a customer purchases a new vehicle, they often request additional accessories such as:

* alloy wheels
* window tint
* reverse camera
* leather steering wheel
* leather seats
* tow hitch
* other accessories

In many dealerships, these requests are communicated through WhatsApp groups that include:

* sales team
* vehicle stock team
* accessories stock team
* managers
* scheduling team

Problems with this approach:

* information gets lost in long conversations
* outdated accessory prices in shared PDFs
* duplicated communication
* lack of structured order tracking
* errors when ordering accessories
* difficult auditing and reporting
* lack of centralized history
* inefficient workflow between departments

---

## Solution

AutoConfig replaces informal communication with a structured workflow system that allows teams to:

* register customer orders
* associate accessories with each order
* maintain updated accessory prices
* link vehicle chassis to the correct order
* centralize communication between departments
* track order history
* improve operational organization
* reduce communication errors
* eliminate dependency on WhatsApp
* generate consistent operational records

The system improves reliability, traceability, and productivity inside dealership workflows.

---

## Target Users

The system is designed for different roles inside the dealership:

* administrators
* managers
* sales team
* vehicle stock team
* accessories stock team

Each role has specific permissions aligned with dealership workflow responsibilities.

---

## Main Features

* JWT authentication (`/auth/login`)
* Role-based authorization
* RESTful API design
* Layered architecture
* DTO pattern for data validation and transport
* Pagination using Spring Pageable
* Global exception handling
* Audit fields (createdAt, updatedAt)
* OpenAPI / Swagger documentation
* Correlation ID logging (traceId)
* Structured logging configuration
* Flyway database migrations
* Multiple Spring profiles (dev, test, prod)
* CI pipeline with GitHub Actions
* Docker Compose support
* Cloud deployment on Railway

---

## Roles and Permissions

Role-based access control ensures each user only accesses the features relevant to their responsibilities.

Roles currently implemented:

* ROLE_ADMIN
* ROLE_MANAGER
* ROLE_SELLER
* ROLE_VEHICLE_STOCK
* ROLE_ACCESSORY_STOCK

Authorization rules are defined at the endpoint level via Spring Security and reinforced at the service layer.

---

## Architecture

The backend follows a layered architecture based on separation of concerns, promoting maintainability, scalability, and testability.

Layered architecture:

Controller layer → handles HTTP requests and responses

Service layer → contains business rules and application logic

Repository layer → responsible for database communication

DTO layer → data transport between layers

Entity layer → represents database tables

Config layer → application and security configurations

Exception layer → centralized error handling


Business rules are isolated in the service layer, ensuring controllers remain lightweight and focused on request handling.

---

## Repository Structure

Root directory contains the backend application.

Frontend is located inside:

frontend/

CI configuration:

.github/workflows/ci.yml

Optional local MySQL container:

compose.yaml

---

## Technologies

### Backend

* Java 17
* Spring Boot 3
* Spring Security
* JWT Authentication
* Spring Data JPA
* Hibernate
* Flyway
* H2 Database (development)
* MySQL (production)
* Maven
* Swagger / OpenAPI

### Frontend

* React
* TypeScript
* Vite
* ESLint

### DevOps

* Git
* GitHub
* GitHub Actions (CI)
* Docker
* Docker Compose
* Railway (cloud deployment)

---

## Environments

### Development (dev)

Default local environment uses:

* H2 in-memory database
* optional automatic admin user for testing
* fast setup for development

### Production (prod)

Production environment uses:

* MySQL database
* environment variables for credentials
* secure JWT configuration

---

## Running locally

### Backend

```bash
./mvnw spring-boot:run
```

or

```bash
mvn spring-boot:run
```

Default profile:

dev

---

### Frontend

```bash
cd frontend
npm install
npm run dev
```

---

## API Documentation

Swagger UI available at:

```bash
/swagger-ui.html
```

OpenAPI JSON:

```bash
/v3/api-docs
```

JWT authentication can be configured directly inside Swagger UI.

---

## CI Pipeline

GitHub Actions pipeline runs automatically on:

* push to main branch
* pull requests

Pipeline steps:

Backend:

* build project
* run unit tests
* validate compilation

Frontend:

* install dependencies
* run build
* run lint validation

Workflow file:

.github/workflows/ci.yml

---

## Project Status

Backend:

implemented and tested

Frontend:

login page implemented
structure prepared for upcoming features

Next planned features:

* customer order creation UI
* accessory catalog visualization
* order workflow tracking
* vehicle association via chassis
* reporting dashboard

---

## Author

This project was designed to solve real operational workflow challenges involving multiple business roles and complex communication flows.

It demonstrates solid backend engineering practices such as:

- layered architecture
- stateless authentication with JWT
- role-based authorization
- DTO pattern
- database migrations with Flyway
- CI pipeline with GitHub Actions
- environment-based configuration
- scalable and maintainable code structure

The goal of the project is to apply software engineering principles to a real business scenario, improving organization, traceability, and operational efficiency.
