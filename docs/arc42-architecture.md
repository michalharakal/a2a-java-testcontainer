# A2A Java Testcontainer - Architecture Documentation (arc42)

**Version:** 1.0  
**Date:** 2025-09-02  
**Authors:** Architecture Team  

---

## Table of Contents

1. [Introduction and Goals](#1-introduction-and-goals)
2. [Architecture Constraints](#2-architecture-constraints)  
3. [System Scope and Context](#3-system-scope-and-context)
4. [Solution Strategy](#4-solution-strategy)
5. [Building Block View](#5-building-block-view)
6. [Runtime View](#6-runtime-view)
7. [Deployment View](#7-deployment-view)
8. [Cross-cutting Concepts](#8-cross-cutting-concepts)
9. [Design Decisions](#9-design-decisions)
10. [Quality Requirements](#10-quality-requirements)
11. [Risks and Technical Debts](#11-risks-and-technical-debts)
12. [Glossary](#12-glossary)

---

## 1. Introduction and Goals

### 1.1 Requirements Overview

The A2A Java Testcontainer project provides a Docker-based integration testing solution for the A2A (Agent2Agent) protocol. It enables developers to easily test their A2A client implementations against a containerized A2A server instance.

**Key Requirements:**
- Provide simple Java API for starting/stopping A2A server containers
- Support health monitoring and readiness detection
- Enable flexible configuration with custom Docker images and environment variables
- Offer rich API for agent interactions, message sending, and health checks
- Support multiple concurrent A2A server instances
- Integrate seamlessly with existing testing frameworks

### 1.2 Quality Goals

| Priority | Quality Goal | Scenario |
|----------|--------------|----------|
| 1 | **Usability** | Developers can set up A2A server integration tests with minimal configuration |
| 2 | **Reliability** | Container startup and health checks work consistently across environments |
| 3 | **Flexibility** | Support various A2A server configurations and multiple concurrent instances |
| 4 | **Performance** | Fast container startup and efficient resource usage during tests |

### 1.3 Stakeholders

| Role | Contact | Expectations |
|------|---------|--------------|
| Java Developers | Development Teams | Easy-to-use API for A2A integration testing |
| QA Engineers | Test Teams | Reliable and consistent test environment |
| DevOps Engineers | Infrastructure Teams | Containerized solution that integrates with CI/CD |
| A2A Protocol Users | Client Implementers | Reference implementation for testing A2A clients |

---

## 2. Architecture Constraints

### 2.1 Technical Constraints

| Constraint | Description |
|------------|-------------|
| **Java Version** | Minimum Java 11+ required |
| **Docker** | Docker engine must be available for container execution |
| **Testcontainers** | Built on Testcontainers framework |
| **A2A Protocol** | Implements A2A protocol version 0.3.0 |
| **Quarkus** | A2A server runs on Quarkus framework |

### 2.2 Organizational Constraints

| Constraint | Description |
|------------|-------------|
| **Open Source** | Project follows open source development practices |
| **Maven** | Uses Maven for build and dependency management |
| **Testing Focus** | Designed specifically for integration testing scenarios |

---

## 3. System Scope and Context

### 3.1 Business Context

```mermaid
graph TB
    subgraph "Development Environment"
        DEV[Java Developer]
        IDE[IDE/Editor]
        TEST[Test Suite]
    end
    
    subgraph "A2A Java Testcontainer"
        CONTAINER[A2AServerContainer]
        API[Container API]
    end
    
    subgraph "Docker Environment"
        DOCKER[Docker Engine]
        IMAGE[A2A Server Image]
        SERVER[A2A Server Instance]
    end
    
    subgraph "A2A Client Under Test"
        CLIENT[A2A Client]
        IMPL[Client Implementation]
    end
    
    DEV --> IDE
    IDE --> TEST
    TEST --> API
    API --> CONTAINER
    CONTAINER --> DOCKER
    DOCKER --> IMAGE
    IMAGE --> SERVER
    TEST --> CLIENT
    CLIENT --> SERVER
    CLIENT --> IMPL
    
    classDef primary fill:#e1f5fe
    classDef secondary fill:#f3e5f5
    classDef external fill:#e8f5e8
    
    class CONTAINER,API primary
    class TEST,CLIENT secondary
    class DOCKER,IMAGE,SERVER external
```

### 3.2 Technical Context

```mermaid
graph LR
    subgraph "Test Environment"
        TEST[JUnit Test]
        TCAPI[Testcontainer API]
    end
    
    subgraph "A2A Testcontainer"
        A2ACONTAINER[A2AServerContainer]
        HTTP[HTTP Client]
    end
    
    subgraph "Docker"
        DOCKER[Docker Engine]
        A2AIMAGE[A2A Server Image]
        INSTANCE[Container Instance]
    end
    
    subgraph "A2A Server"
        QUARKUS[Quarkus Runtime]
        ENDPOINTS[REST Endpoints]
        AGENT[Hello World Agent]
    end
    
    TEST --> TCAPI
    TCAPI --> A2ACONTAINER
    A2ACONTAINER --> HTTP
    A2ACONTAINER --> DOCKER
    DOCKER --> A2AIMAGE
    A2AIMAGE --> INSTANCE
    INSTANCE --> QUARKUS
    QUARKUS --> ENDPOINTS
    ENDPOINTS --> AGENT
    HTTP --> ENDPOINTS
    
    classDef testLayer fill:#e3f2fd
    classDef containerLayer fill:#e8f5e8
    classDef dockerLayer fill:#fff3e0
    classDef serverLayer fill:#fce4ec
    
    class TEST,TCAPI testLayer
    class A2ACONTAINER,HTTP containerLayer
    class DOCKER,A2AIMAGE,INSTANCE dockerLayer
    class QUARKUS,ENDPOINTS,AGENT serverLayer
```

---

## 4. Solution Strategy

### 4.1 Architecture Approach

The solution follows a **Wrapper Pattern** approach, encapsulating Docker container management within a Java API that extends the Testcontainers framework.

**Key Strategies:**
- **Container Abstraction**: Hide Docker complexity behind simple Java methods
- **Health Monitoring**: Implement robust health checks and readiness detection
- **HTTP Communication**: Use standard HTTP clients for A2A protocol communication
- **Configuration Flexibility**: Support environment variables and custom images
- **Test Integration**: Design API specifically for testing scenarios

### 4.2 Technology Stack

```mermaid
graph TB
    subgraph "Application Layer"
        API[A2AServerContainer API]
        TESTS[JUnit Tests]
    end
    
    subgraph "Integration Layer"
        TC[Testcontainers Framework]
        HTTP[Apache HTTP Client]
        JSON[Jackson JSON]
    end
    
    subgraph "Container Layer"
        DOCKER[Docker Engine]
        IMAGE[Multi-stage Build]
    end
    
    subgraph "Server Layer"
        QUARKUS[Quarkus Framework]
        A2ASDK[A2A Java SDK]
        JVM[Java 17 Runtime]
    end
    
    API --> TC
    API --> HTTP
    API --> JSON
    TESTS --> API
    TC --> DOCKER
    DOCKER --> IMAGE
    IMAGE --> QUARKUS
    QUARKUS --> A2ASDK
    A2ASDK --> JVM
    
    classDef app fill:#e1f5fe
    classDef integration fill:#e8f5e8
    classDef container fill:#fff3e0
    classDef server fill:#fce4ec
    
    class API,TESTS app
    class TC,HTTP,JSON integration
    class DOCKER,IMAGE container
    class QUARKUS,A2ASDK,JVM server
```

---

## 5. Building Block View

### 5.1 Level 1 - System Overview

```mermaid
graph TB
    subgraph "A2A Java Testcontainer System"
        CONTAINER[A2AServerContainer]
        CONFIG[Configuration Management]
        HEALTH[Health Monitoring]
        COMM[Communication Layer]
    end
    
    subgraph "External Systems"
        DOCKER[Docker Engine]
        A2ASERVER[A2A Server]
        TESTS[Test Framework]
    end
    
    TESTS --> CONTAINER
    CONTAINER --> CONFIG
    CONTAINER --> HEALTH
    CONTAINER --> COMM
    CONTAINER --> DOCKER
    DOCKER --> A2ASERVER
    COMM --> A2ASERVER
    HEALTH --> A2ASERVER
    
    classDef internal fill:#e1f5fe
    classDef external fill:#e8f5e8
    
    class CONTAINER,CONFIG,HEALTH,COMM internal
    class DOCKER,A2ASERVER,TESTS external
```

### 5.2 Level 2 - A2AServerContainer Details

```mermaid
classDiagram
    class A2AServerContainer {
        -ObjectMapper objectMapper
        -CloseableHttpClient httpClient
        +A2AServerContainer()
        +A2AServerContainer(String imageName)
        +start()
        +stop()
        +getServerUrl() String
        +getPublicAgentCard() JsonNode
        +sendMessage(String) String
        +isHealthy() boolean
        +waitForReady(Duration)
    }
    
    class GenericContainer {
        <<Testcontainers>>
        +withExposedPorts()
        +withEnv()
        +waitingFor()
    }
    
    class HttpClients {
        <<Apache_HTTP>>
        +createDefault()
        +execute()
    }
    
    class ObjectMapper {
        <<Jackson>>
        +readTree()
        +writeValueAsString()
    }
    
    A2AServerContainer --|> GenericContainer
    A2AServerContainer --> HttpClients
    A2AServerContainer --> ObjectMapper
```

### 5.3 Level 3 - Docker Image Structure

```mermaid
graph TB
    subgraph "Multi-stage Docker Build"
        subgraph "Builder Stage"
            MAVEN[Maven 3.9 + JDK 17]
            CLONE[Git Clone a2a-java]
            BUILD[Maven Build]
            PACKAGE[Package HelloWorld Server]
        end
        
        subgraph "Runtime Stage"
            JRE[Eclipse Temurin 17 JRE]
            APP[Quarkus Application]
            USER[Non-root User]
            HEALTH[Health Check]
        end
    end
    
    MAVEN --> CLONE
    CLONE --> BUILD
    BUILD --> PACKAGE
    PACKAGE --> JRE
    JRE --> APP
    APP --> USER
    USER --> HEALTH
    
    classDef build fill:#fff3e0
    classDef runtime fill:#e8f5e8
    
    class MAVEN,CLONE,BUILD,PACKAGE build
    class JRE,APP,USER,HEALTH runtime
```

---

## 6. Runtime View

### 6.1 Container Startup Sequence

```mermaid
sequenceDiagram
    participant Test as JUnit Test
    participant Container as A2AServerContainer
    participant Docker as Docker Engine
    participant Server as A2A Server
    
    Test->>Container: new A2AServerContainer()
    Container->>Container: Configure ports & environment
    Test->>Container: start()
    Container->>Docker: Start container with image
    Docker->>Server: Launch Quarkus application
    Server->>Server: Initialize A2A agent
    Server->>Container: Health check endpoint ready
    Container->>Container: Wait for health check
    Container->>Server: GET /q/health
    Server-->>Container: 200 OK
    Container->>Container: Initialize HTTP client
    Container-->>Test: Container ready
```

### 6.2 Message Exchange Flow

```mermaid
sequenceDiagram
    participant Test as Test Code
    participant Container as A2AServerContainer
    participant Server as A2A Server
    participant Agent as Hello World Agent
    
    Test->>Container: sendMessage("Hello!")
    Container->>Container: Create JSON-RPC request
    Container->>Server: POST /a2a/invoke
    Server->>Agent: execute(input="Hello!")
    Agent->>Agent: Process message
    Agent-->>Server: "Hello World" response
    Server-->>Container: JSON-RPC response
    Container->>Container: Parse response
    Container-->>Test: Return response string
```

### 6.3 Health Monitoring Flow

```mermaid
sequenceDiagram
    participant Container as A2AServerContainer
    participant Server as A2A Server
    participant Quarkus as Quarkus Health
    
    loop Health Check
        Container->>Server: GET /q/health
        Server->>Quarkus: Check application health
        Quarkus-->>Server: Health status
        alt Healthy
            Server-->>Container: 200 OK
        else Unhealthy
            Server-->>Container: 503 Service Unavailable
        end
    end
```

---

## 7. Deployment View

### 7.1 Development Environment

```mermaid
graph TB
    subgraph "Developer Machine"
        subgraph "IDE Environment"
            IDE[IntelliJ IDEA / VS Code]
            MAVEN[Maven Build Tool]
            TESTS[JUnit Test Suite]
        end
        
        subgraph "Docker Desktop"
            ENGINE[Docker Engine]
            IMAGES[Container Images]
            CONTAINERS[Running Containers]
        end
        
        subgraph "A2A Testcontainer"
            JAR[Testcontainer JAR]
            CONFIG[Test Configuration]
        end
    end
    
    IDE --> MAVEN
    MAVEN --> JAR
    TESTS --> JAR
    JAR --> ENGINE
    ENGINE --> IMAGES
    IMAGES --> CONTAINERS
    
    classDef dev fill:#e1f5fe
    classDef docker fill:#fff3e0
    classDef container fill:#e8f5e8
    
    class IDE,MAVEN,TESTS dev
    class ENGINE,IMAGES,CONTAINERS docker
    class JAR,CONFIG container
```

### 7.2 CI/CD Pipeline

```mermaid
graph LR
    subgraph "Source Control"
        GIT[Git Repository]
        PR[Pull Request]
    end
    
    subgraph "CI Pipeline"
        BUILD[Maven Build]
        TEST[Integration Tests]
        DOCKER[Docker Available]
    end
    
    subgraph "Container Registry"
        REGISTRY[Docker Registry]
        A2AIMAGE[A2A Server Image]
    end
    
    GIT --> BUILD
    PR --> BUILD
    BUILD --> TEST
    TEST --> DOCKER
    BUILD --> REGISTRY
    REGISTRY --> A2AIMAGE
    DOCKER --> A2AIMAGE
    
    classDef source fill:#e8f5e8
    classDef ci fill:#e1f5fe
    classDef registry fill:#fff3e0
    
    class GIT,PR source
    class BUILD,TEST,DOCKER ci
    class REGISTRY,A2AIMAGE registry
```

### 7.3 Container Infrastructure

```mermaid
graph TB
    subgraph "Host Machine"
        subgraph "Docker Engine"
            NETWORK[Bridge Network]
            VOLUME[Container Volumes]
        end
        
        subgraph "A2A Server Container"
            QUARKUS[Quarkus App :9999]
            HEALTH[Health Check]
            USER[a2a user]
        end
        
        subgraph "Test Process"
            JVM[Test JVM]
            HTTP[HTTP Client]
        end
    end
    
    JVM --> HTTP
    HTTP --> NETWORK
    NETWORK --> QUARKUS
    QUARKUS --> HEALTH
    QUARKUS --> USER
    USER --> VOLUME
    
    classDef host fill:#f5f5f5
    classDef container fill:#e8f5e8
    classDef test fill:#e1f5fe
    
    class NETWORK,VOLUME host
    class QUARKUS,HEALTH,USER container
    class JVM,HTTP test
```

---

## 8. Cross-cutting Concepts

### 8.1 Logging and Monitoring

```mermaid
graph TB
    subgraph "Logging Layers"
        APP[Application Logs]
        CONTAINER[Container Logs]
        DOCKER[Docker Logs]
    end
    
    subgraph "Log Consumers"
        CONSOLE[Console Output]
        FILE[Log Files]
        CUSTOM[Custom Consumers]
    end
    
    APP --> CONSOLE
    CONTAINER --> CONSOLE
    DOCKER --> CONSOLE
    APP --> FILE
    CONTAINER --> FILE
    APP --> CUSTOM
    
    classDef logs fill:#e1f5fe
    classDef output fill:#e8f5e8
    
    class APP,CONTAINER,DOCKER logs
    class CONSOLE,FILE,CUSTOM output
```

### 8.2 Error Handling

| Layer | Error Type | Handling Strategy |
|-------|------------|-------------------|
| **Container** | Docker startup failures | Retry with timeout, detailed error messages |
| **HTTP Communication** | Connection errors | HTTP client retries, IOException wrapping |
| **Health Checks** | Server unavailable | Graceful degradation, boolean return |
| **JSON Processing** | Parse errors | Jackson exception handling, error logging |

### 8.3 Configuration Management

```mermaid
graph TB
    subgraph "Configuration Sources"
        DEFAULT[Default Values]
        ENV[Environment Variables]
        CUSTOM[Custom Configuration]
    end
    
    subgraph "Configuration Properties"
        PORT[Port Mapping]
        IMAGE[Docker Image]
        TIMEOUT[Health Timeout]
        QUARKUS[Quarkus Settings]
    end
    
    DEFAULT --> PORT
    DEFAULT --> IMAGE
    DEFAULT --> TIMEOUT
    ENV --> QUARKUS
    CUSTOM --> IMAGE
    CUSTOM --> ENV
    
    classDef source fill:#e1f5fe
    classDef config fill:#e8f5e8
    
    class DEFAULT,ENV,CUSTOM source
    class PORT,IMAGE,TIMEOUT,QUARKUS config
```

---

## 9. Design Decisions

### 9.1 Technology Choices

| Decision | Rationale | Alternatives Considered |
|----------|-----------|------------------------|
| **Testcontainers Framework** | Industry standard for container-based testing | Custom Docker integration |
| **Apache HTTP Client** | Mature, reliable HTTP communication | OkHttp, Java 11 HTTP Client |
| **Jackson JSON** | De facto standard for JSON processing in Java | Gson, native JSON processing |
| **Quarkus for A2A Server** | Fast startup, cloud-native Java framework | Spring Boot, traditional Jakarta EE |

### 9.2 Architecture Decisions

```mermaid
graph TB
    subgraph "Design Decisions"
        EXTEND[Extend GenericContainer]
        HTTP[HTTP Communication]
        HEALTH[Health Check Strategy]
        CONFIG[Configuration Approach]
    end
    
    subgraph "Benefits"
        REUSE[Reuse Testcontainers Features]
        STANDARD[Standard HTTP Protocol]
        ROBUST[Robust Startup Detection]
        FLEXIBLE[Flexible Configuration]
    end
    
    EXTEND --> REUSE
    HTTP --> STANDARD
    HEALTH --> ROBUST
    CONFIG --> FLEXIBLE
    
    classDef decision fill:#e1f5fe
    classDef benefit fill:#e8f5e8
    
    class EXTEND,HTTP,HEALTH,CONFIG decision
    class REUSE,STANDARD,ROBUST,FLEXIBLE benefit
```

### 9.3 Trade-offs

| Trade-off | Decision | Rationale |
|-----------|----------|-----------|
| **Startup Time vs Reliability** | Longer timeout (3 minutes) | Ensures reliable container startup across environments |
| **API Simplicity vs Flexibility** | Simple API with extension points | Easy to use for common cases, extensible for complex scenarios |
| **Resource Usage vs Isolation** | One container per test instance | Better isolation, acceptable resource overhead for testing |

---

## 10. Quality Requirements

### 10.1 Quality Tree

```mermaid
graph TB
    QUALITY[Quality Requirements]
    
    QUALITY --> USABILITY
    QUALITY --> RELIABILITY
    QUALITY --> PERFORMANCE
    QUALITY --> MAINTAINABILITY
    
    USABILITY --> SIMPLE[Simple API]
    USABILITY --> DOCS[Good Documentation]
    USABILITY --> EXAMPLES[Usage Examples]
    
    RELIABILITY --> HEALTH[Health Checks]
    RELIABILITY --> ERROR[Error Handling]
    RELIABILITY --> TIMEOUT[Timeout Management]
    
    PERFORMANCE --> STARTUP[Fast Startup]
    PERFORMANCE --> RESOURCE[Resource Efficiency]
    PERFORMANCE --> CONCURRENT[Concurrent Support]
    
    MAINTAINABILITY --> LOGGING[Comprehensive Logging]
    MAINTAINABILITY --> TESTING[Unit Testing]
    MAINTAINABILITY --> CONFIG[Configurable]
    
    classDef primary fill:#e1f5fe
    classDef secondary fill:#e8f5e8
    
    class QUALITY primary
    class USABILITY,RELIABILITY,PERFORMANCE,MAINTAINABILITY secondary
```

### 10.2 Quality Scenarios

| Quality | Scenario | Response |
|---------|----------|----------|
| **Usability** | Developer needs to test A2A client | Can create and start container with 3 lines of code |
| **Reliability** | Docker daemon stops during test | Container fails gracefully with clear error message |
| **Performance** | Running 10 concurrent containers | Each container starts within 30 seconds, acceptable memory usage |
| **Maintainability** | Need to support new A2A version | Configuration allows custom Docker image specification |

---

## 11. Risks and Technical Debts

### 11.1 Identified Risks

| Risk | Probability | Impact | Mitigation Strategy |
|------|-------------|---------|-------------------|
| **Docker Availability** | Medium | High | Clear error messages, documentation for Docker setup |
| **A2A Server Changes** | Low | Medium | Version pinning, configurable Docker images |
| **Port Conflicts** | Low | Low | Dynamic port allocation by Testcontainers |
| **Resource Consumption** | Medium | Medium | Container cleanup, resource monitoring |

### 11.2 Technical Debts

```mermaid
graph TB
    subgraph "Current Technical Debts"
        HARDCODE[Hardcoded Endpoints]
        LIMITED[Limited Error Messages]
        SINGLE[Single A2A Version]
    end
    
    subgraph "Future Improvements"
        DYNAMIC[Dynamic Endpoint Discovery]
        DETAILED[Detailed Error Reporting]
        MULTI[Multi-version Support]
    end
    
    HARDCODE --> DYNAMIC
    LIMITED --> DETAILED
    SINGLE --> MULTI
    
    classDef debt fill:#ffcdd2
    classDef improvement fill:#e8f5e8
    
    class HARDCODE,LIMITED,SINGLE debt
    class DYNAMIC,DETAILED,MULTI improvement
```

### 11.3 Evolution Path

| Priority | Improvement | Timeline | Effort |
|----------|-------------|----------|--------|
| High | Enhanced error messages | Next release | Low |
| Medium | Multi-version A2A support | 3 months | Medium |
| Low | Performance optimizations | 6 months | High |

---

## 12. Glossary

| Term | Definition |
|------|------------|
| **A2A** | Agent2Agent - A protocol for communication between autonomous agents |
| **Agent Card** | JSON document describing an agent's capabilities and contact information |
| **GenericContainer** | Testcontainers base class for Docker container management |
| **Health Check** | Automated verification that a service is running and ready to accept requests |
| **JSON-RPC** | Remote procedure call protocol encoded in JSON |
| **Quarkus** | Java framework optimized for cloud and containerized applications |
| **Testcontainers** | Java library for integration testing with Docker containers |

---

## Appendix

### A.1 Reference Documentation

- [A2A Java SDK Documentation](https://github.com/a2aproject/a2a-java)
- [Testcontainers Documentation](https://www.testcontainers.org/)
- [Quarkus Framework](https://quarkus.io/)
- [Docker Documentation](https://docs.docker.com/)

### A.2 Related Projects

- A2A Java SDK - Core A2A protocol implementation
- A2A Specification - Official A2A protocol specification
- Testcontainers - Container-based integration testing framework

---

*This document follows the arc42 template for architecture documentation.*