# 🚕 Ride-Sharing Platform — Microservices Backend

A **microservices-based ride-sharing backend** built with **Java and Spring Boot**, inspired by ride-hailing platforms such as Uber.

The system allows riders to request rides, tracks driver locations in real time, finds nearby drivers using Redis GEO, and assigns the most suitable driver based on **distance and rating**.

The project demonstrates **microservices architecture, event-driven communication, Redis geospatial indexing, Apache Kafka, OpenFeign, REST APIs, and PostgreSQL persistence**.

---

## 🏗️ Architecture

The application is divided into three independent microservices:

```text
                         ┌──────────────────┐
                         │      Rider       │
                         └────────┬─────────┘
                                  │
                                  │ Request Ride
                                  ▼
                    ┌──────────────────────────┐
                    │      Ride Service        │
                    │       Port: 8083         │
                    └────────────┬─────────────┘
                                 │
                       Store Ride │ PostgreSQL
                                 │
                                 ▼
                         ┌──────────────┐
                         │    Kafka     │
                         │              │
                         │ ride.requested
                         └──────┬───────┘
                                │
                                ▼
                    ┌──────────────────────────┐
                    │    Matching Service      │
                    │       Port: 8084         │
                    └────────────┬─────────────┘
                                 │
                           OpenFeign
                                 │
                                 ▼
                    ┌──────────────────────────┐
                    │    Location Service      │
                    │       Port: 8082         │
                    └────────────┬─────────────┘
                                 │
                              Redis GEO
                                 │
                                 ▼
                         Nearby Drivers
                                 │
                                 ▼
                         Driver Matching
                                 │
                                 ▼
                         ┌──────────────┐
                         │    Kafka     │
                         │ ride.matched │
                         └──────┬───────┘
                                │
                                ▼
                    ┌──────────────────────────┐
                    │      Ride Service         │
                    │                          │
                    │ Assign Driver + Update   │
                    │ Ride Status              │
                    └──────────────────────────┘
```

---

## 🔄 Ride Request Flow

The complete ride-request workflow is:

```text
1. Rider requests a ride
        ↓
2. Ride Service receives the request
        ↓
3. Ride is persisted in PostgreSQL
        ↓
4. RideRequestedEvent is published to Kafka
        ↓
5. Matching Service consumes the event
        ↓
6. Matching Service requests nearby drivers
   from Location Service using OpenFeign
        ↓
7. Location Service queries Redis GEO
        ↓
8. Nearby drivers are returned
        ↓
9. Matching Service evaluates drivers
   based on distance and rating
        ↓
10. Best driver is selected
        ↓
11. RideMatchedEvent is published to Kafka
        ↓
12. Ride Service consumes the event
        ↓
13. Ride is updated with the assigned driver
        ↓
14. Updated ride is persisted in PostgreSQL
```

---

## 🧩 Microservices

### 📍 Location Service

**Port:** `8082`

Responsible for maintaining real-time driver locations.

Drivers periodically send their:

* Driver ID
* Latitude
* Longitude
* Location information

Driver locations are stored using **Redis GEO**.

The service provides proximity-based driver retrieval using geographic coordinates and a search radius.

Example:

```text
Driver locations
       ↓
Redis GEO
       ↓
Search within radius
       ↓
Nearby drivers
```

---

### 🚕 Ride Service

**Port:** `8083`

Responsible for managing the ride lifecycle.

Responsibilities include:

* Creating ride requests
* Persisting ride information
* Publishing ride-request events
* Consuming driver-matching events
* Assigning drivers to rides
* Updating ride status

Ride information is persisted using **PostgreSQL**.

---

### 🎯 Matching Service

**Port:** `8084`

Responsible for finding the best driver for a ride.

The service:

1. Consumes `RideRequestedEvent` from Kafka.
2. Calls Location Service using **OpenFeign**.
3. Retrieves nearby drivers.
4. Evaluates available drivers based on:

   * Distance
   * Driver rating
5. Selects the most suitable driver.
6. Publishes a `RideMatchedEvent`.

---

## 📨 Kafka Event Flow

The project uses **Apache Kafka** for asynchronous communication between services.

### `ride.requested`

Published by:

```text
Ride Service
```

Consumed by:

```text
Matching Service
```

The event contains the information required to process a ride request.

```text
Ride Service
      │
      │ RideRequestedEvent
      ▼
ride.requested
      │
      ▼
Matching Service
```

### `ride.matched`

Published by:

```text
Matching Service
```

Consumed by:

```text
Ride Service
```

```text
Matching Service
      │
      │ RideMatchedEvent
      ▼
ride.matched
      │
      ▼
Ride Service
```

This allows the services to communicate asynchronously without being tightly coupled.

---

## 📡 Driver Location Tracking

Driver locations are updated periodically:

```text
Driver
  │
  │ Location update
  │ every 3 seconds
  ▼
Location Service
  │
  ▼
Redis GEO
```

Redis GEO allows the application to efficiently perform proximity searches.

For example:

```text
Rider Location
      ●
     /|\
    / | \
   /  |  \
  ●   ●   ●
 D1  D2  D3

Search Radius
      ↓
Nearby Drivers
```

The Matching Service can then evaluate these nearby drivers instead of searching through every driver in the system.

---

## 🎯 Driver Matching

The Matching Service receives nearby drivers from the Location Service and evaluates them based on:

* Driver distance from pickup location
* Driver rating

The goal is to select the **most suitable available driver** for the requested ride.

Conceptually:

```text
Nearby Drivers
      │
      ├── Driver A → 1.2 km → ⭐ 4.5
      ├── Driver B → 0.8 km → ⭐ 4.2
      ├── Driver C → 1.5 km → ⭐ 4.9
      └── Driver D → 0.9 km → ⭐ 4.6
                       │
                       ▼
                Matching Algorithm
                       │
                       ▼
                 Best Driver
```

---

## 🔗 Service Communication

The project uses two different communication patterns.

### Synchronous — OpenFeign

Used when Matching Service needs information from Location Service:

```text
Matching Service
       │
       │ HTTP / OpenFeign
       ▼
Location Service
       │
       ▼
Nearby Drivers
```

### Asynchronous — Kafka

Used for ride lifecycle events:

```text
Ride Service
     │
     │ RideRequestedEvent
     ▼
   Kafka
     │
     ▼
Matching Service
```

and:

```text
Matching Service
     │
     │ RideMatchedEvent
     ▼
   Kafka
     │
     ▼
Ride Service
```

---

## 🛠️ Tech Stack

| Technology                      | Purpose                                      |
| ------------------------------- | -------------------------------------------- |
| **Java**                        | Programming language                         |
| **Spring Boot**                 | Microservice development                     |
| **Spring Data JPA / Hibernate** | Database persistence                         |
| **PostgreSQL**                  | Persistent ride data                         |
| **Redis**                       | Real-time driver location storage            |
| **Redis GEO**                   | Geospatial proximity searches                |
| **Apache Kafka**                | Event-driven communication                   |
| **Spring Kafka**                | Kafka integration                            |
| **OpenFeign**                   | Synchronous service-to-service communication |
| **REST APIs**                   | Client/service APIs                          |
| **Spring Boot Actuator**        | Application health and monitoring            |
| **Docker**                      | Infrastructure/containerization              |

---

## 📁 Project Structure

```text
ride-sharing-platform/
│
├── ride-service/
│   ├── src/
│   └── pom.xml
│
├── location-service/
│   ├── src/
│   └── pom.xml
│
├── matching-service/
│   ├── src/
│   └── pom.xml
│
└── README.md
```

Each service is independently developed and can be started separately.

---

## ⚙️ Configuration

### Ride Service

```properties
server.port=8083
spring.application.name=ride-service
```

### Location Service

```properties
server.port=8082
spring.application.name=location-service
```

### Matching Service

```properties
server.port=8084
spring.application.name=matching-service
```

Kafka runs on:

```text
localhost:9092
```

Redis runs on:

```text
localhost:6379
```

---

## 🚀 Running the Project

### Prerequisites

Make sure the following are installed:

* Java 17+
* Maven
* PostgreSQL
* Redis
* Apache Kafka
* Docker (optional)

### 1. Start PostgreSQL

Create the required database:

```sql
CREATE DATABASE uberapp;
```

### 2. Start Redis

```bash
redis-server
```

or using Docker:

```bash
docker run -d \
  --name redis \
  -p 6379:6379 \
  redis
```

### 3. Start Kafka

Start your Kafka broker on:

```text
localhost:9092
```

### 4. Start the services

Start each Spring Boot application:

```bash
cd location-service
mvn spring-boot:run
```

```bash
cd ride-service
mvn spring-boot:run
```

```bash
cd matching-service
mvn spring-boot:run
```

---

## 📊 Service Ports

| Service          |   Port |
| ---------------- | -----: |
| Location Service | `8082` |
| Ride Service     | `8083` |
| Matching Service | `8084` |
| Kafka            | `9092` |
| Redis            | `6379` |
| PostgreSQL       | `5432` |

---

## ❤️ Health Monitoring

Spring Boot Actuator is enabled for service health checks.

```text
GET /actuator/health
GET /actuator/info
```

Example:

```text
http://localhost:8082/actuator/health
http://localhost:8083/actuator/health
http://localhost:8084/actuator/health
```

---

## 📚 Key Concepts Demonstrated

This project was built to explore practical backend and distributed-systems concepts including:

* Microservices architecture
* Service-to-service communication
* Event-driven architecture
* Asynchronous messaging
* Apache Kafka
* Kafka producers and consumers
* Kafka topics and partitions
* Consumer groups
* Redis geospatial indexing
* Real-time location tracking
* Proximity-based search
* Driver matching algorithms
* RESTful API design
* PostgreSQL persistence
* OpenFeign
* Spring Boot Actuator
* Distributed service workflows

---

## 🔮 Future Improvements

Potential improvements for a production-grade version include:

* API Gateway
* Service discovery
* Authentication and authorization
* Distributed tracing
* Centralized configuration
* Kafka replication and fault tolerance
* Driver availability management
* Concurrent driver assignment protection
* Redis-based distributed locking
* Retry and dead-letter handling for Kafka events
* Dynamic pricing / surge pricing
* Payment service
* Notification service
* Containerized deployment
* Kubernetes orchestration

---

## 👨‍💻 Author

**Imran**

Built as a backend engineering project to explore **Java, Spring Boot, microservices, distributed communication, Kafka, Redis, and real-time location-based systems**.
