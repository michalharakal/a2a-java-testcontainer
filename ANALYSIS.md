# A2A Java Server Analysis

## Server Overview
The a2a-java helloworld server is a Quarkus-based application that implements an A2A (Agent2Agent) protocol server.

## Key Components

### Dependencies (from pom.xml)
- **Quarkus Framework**: Uses quarkus-resteasy-jackson for REST endpoints
- **A2A SDK**: 
  - `a2a-java-sdk-client` - Core client functionality
  - `a2a-java-sdk-reference-jsonrpc` - JSON-RPC implementation
- **Jakarta EE**: CDI and JAX-RS APIs
- **Parent**: `a2a-java-sdk-examples-parent` version `0.3.0.Alpha1`

### Java Classes
1. **AgentCardProducer.java**
   - CDI Producer for AgentCard
   - Defines agent capabilities (streaming, push notifications, state transition history)
   - Sets up "Hello World Agent" with single skill
   - Configures default URL as `http://localhost:9999`

2. **AgentExecutorProducer.java**
   - CDI Producer for AgentExecutor
   - Implements core logic that returns "Hello World" message
   - Handles execute() method, throws UnsupportedOperationError for cancel()

### Configuration
- **application.properties**: Sets dev mode port to 9999
- **Build**: Uses Quarkus Maven plugin for building
- **Runtime**: Requires Java 11+

### Server Behavior
- Exposes A2A protocol endpoints via REST
- Responds to agent requests with "Hello World" message
- Supports streaming responses
- Provides agent card with capabilities and skill definitions

## Testcontainer Requirements

### Docker Image
- Base: Java 11+ runtime (OpenJDK recommended)
- Maven for building the application
- Quarkus application in JVM mode

### Container Configuration
- **Port**: Expose 9999 (configurable)
- **Health Check**: Wait for Quarkus to be ready
- **Environment**: Support for Quarkus profile configuration
- **Network**: Allow external connections

### Java Testcontainer Wrapper
- Extend GenericContainer or create custom container
- Provide methods to:
  - Configure port mapping
  - Wait for server readiness
  - Get server URL for client connections
  - Access agent card endpoint
  - Support custom Quarkus configurations