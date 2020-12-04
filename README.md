# Medicine App - Microservices architecture
## Table of Contents
* [General info](#general-info)
* [Components](#components)
* [Architecture Diagram](#architecture-diagram)
* [Technologies](#technologies)
* [Scope of functionalities](#scope-of-functionalities)
* [State Machine Diagram](#state-machine-diagram)
* [States](#states)
* [Events](#events)
* [Project Status](#project-status)

## General Info
The project implements the Microservices Architecture in a Medicine App with 3 main services (medicine service, inventory service and order service). 
## Components
| Project | Port |
| ------ | ------ |
| [medicine-inventory-service](https://github.com/AndreasPr/medicine-inventory-service) | 8082 |
| [medicine-order-service](https://github.com/AndreasPr/medicine-order-service) | 8081 |
| [medicine-service](https://github.com/AndreasPr/medicine-service) | 8080 |
| [medicine-production-eureka](https://github.com/AndreasPr/medicine-production-eureka) | 8761 |
| [medicine-production-gateway](https://github.com/AndreasPr/medicine-production-gateway) | 9090 |
| [medicine-config-server](https://github.com/AndreasPr/medicine-config-server) | 8888 |
| [medicine-inventory-failover](https://github.com/AndreasPr/medicine-inventory-failover) | 8083 |
| [medicine-production-config-repo](https://github.com/AndreasPr/medicine-production-config-repo) | - |
| [medicine-production-bom](https://github.com/AndreasPr/medicine-production-bom) | - |

## Architecture Diagram
![photo1](https://github.com/AndreasPr/medicine-order-service/blob/master/medicine-microservices.png)

## Technologies
* Spring Boot
* Spring Web
* Spring Data JPA & Hibernate
* Spring RestTemplate
* Spring Cloud OpenFeign
* Spring Cloud Config
* Spring Cloud Gateway
* Spring Security
* Spring State Machine
* Spring Validation
* Spring TaskExecutor
* Microservices Saga Pattern & compensating transactions
* Circuit Breaker Pattern using Hystrix
* Service Registration using Netflix Eureka
* Service Discovery using Netflix Eureka
* H2 Database
* MySQL
* HikariCP
* Project Lombok
* MapStruct
* Jackson JSON
* JUnit5
* Integration Tests
* Awaitility
* WireMock
* JMS Messaging
* ActiveMQ Artemis
* Apache Maven
* Enterprise Dependency Management & BOM

## Scope of functionalities
* #### Medicine Inventory:
    * Management of the medicine inventory
    * Through production operations, it receives inventory
    * Provision of inventory for orders
  
* #### Medicine Service:
    * Management of the medicines 
    * Management of the production - manufacturing
    * Listing of the medicines
    * Validation of the Order Lines for valid medicines
* #### Medicine Order Service:
    * Management of the medicine orders
    * Management of the lifecycle of an order, from the stage of the placement to order shipment
    * Management of the customers
    * Listening to order events

## State Machine Diagram
![photo2](https://github.com/AndreasPr/medicine-order-service/blob/master/state%20machine%20inventory.png)

## States
| States |
| ------ |
| New | 
| Validation Pending |
| Validated |
| Cancelled |
| Validation Exception |
| Allocation Pending |
| Allocated |
| Allocation Exception |
| Pending Inventory |
| Picked Up |

## Events

| Events |
| ------ |
| VALIDATE ORDER | 
| VALIDATION PASSED |
| CANCEL ORDER |
| VALIDATION FAILED |
| ALLOCATE ORDER |
| ALLOCATION SUCCESS |
| ALLOCATION FAILED |
| ALLOCATION NO INVENTORY |
| MEDICINE ORDER PICKED UP |

## Project Status
The project is still being developed.
