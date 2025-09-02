# A2A Java Testcontainer

A Testcontainer implementation for the [A2A (Agent2Agent) Java Server](https://github.com/a2aproject/a2a-java), providing an easy way to integration test A2A protocol implementations.

## Overview

This project provides a Docker-based testcontainer that wraps the A2A Java helloworld server, making it simple to start and test against an A2A server instance in your integration tests.

## Features

- **Easy Integration**: Simple Java API for starting/stopping A2A server containers
- **Health Checks**: Built-in health monitoring and readiness detection  
- **Flexible Configuration**: Support for custom Docker images and environment variables
- **Rich API**: Methods for interacting with agent cards, sending messages, and health checks
- **Multi-container Support**: Run multiple A2A server instances simultaneously
- **Test-friendly**: Designed specifically for integration testing scenarios

## Quick Start

### Prerequisites

- Java 11 or higher
- Docker
- Maven

### Installation

Add the dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>io.a2a.testcontainers</groupId>
    <artifactId>a2a-java-testcontainer</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <scope>test</scope>
</dependency>
```

### Build Docker Image

First, build the A2A server Docker image:

```bash
docker build -t a2a-java-server:latest .
```

### Basic Usage

```java
import io.a2a.testcontainers.A2AServerContainer;
import com.fasterxml.jackson.databind.JsonNode;

@Test
void testA2AServer() throws IOException {
    try (A2AServerContainer container = new A2AServerContainer()) {
        container.start();
        
        // Get server URL for your client
        String serverUrl = container.getServerUrl();
        
        // Get agent information  
        JsonNode agentCard = container.getPublicAgentCard();
        assertEquals("Hello World Agent", agentCard.get("name").asText());
        
        // Send message to agent
        String response = container.sendMessage("Hello!");
        assertTrue(response.contains("Hello World"));
        
        // Use serverUrl with your A2A client for testing
        // YourA2AClient client = new YourA2AClient(serverUrl);
        // client.connect();
        // String result = client.query("test message");
    }
}
```

## API Reference

### A2AServerContainer

#### Constructors
- `A2AServerContainer()` - Create container with default image
- `A2AServerContainer(String dockerImageName)` - Create container with custom image

#### Key Methods
- `start()` - Start the container and wait for readiness
- `stop()` - Stop the container
- `getServerUrl()` - Get the base URL of the A2A server
- `getPublicAgentCard()` - Get the agent's public card as JsonNode
- `sendMessage(String message)` - Send a message and get response
- `isHealthy()` - Check if server is healthy
- `waitForReady(Duration timeout)` - Wait for server with custom timeout

#### Configuration Methods
- `withEnv(String key, String value)` - Add environment variable
- `withLogConsumer(Consumer<OutputFrame> logConsumer)` - Capture logs

## Advanced Usage

### Custom Configuration

```java
A2AServerContainer container = new A2AServerContainer()
    .withEnv("QUARKUS_LOG_LEVEL", "DEBUG")
    .withEnv("CUSTOM_SETTING", "value")
    .withLogConsumer(frame -> System.out.print("[A2A] " + frame.getUtf8String()));
```

### Multiple Containers

```java
@Test
void testMultipleServers() {
    A2AServerContainer server1 = new A2AServerContainer();
    A2AServerContainer server2 = new A2AServerContainer();
    
    try {
        server1.start();
        server2.start();
        
        // Different ports automatically assigned
        assertNotEquals(server1.getMappedPort(9999), server2.getMappedPort(9999));
        
        // Test both servers
        assertTrue(server1.isHealthy());
        assertTrue(server2.isHealthy());
        
    } finally {
        server1.stop();
        server2.stop();
    }
}
```

### Skip Integration Tests

Set environment variable to skip tests requiring Docker:

```bash
export SKIP_INTEGRATION_TESTS=true
mvn test
```

## Building from Source

```bash
# Clone the repository
git clone <repository-url>
cd a2a-java-testcontainer

# Build the Docker image
docker build -t a2a-java-server:latest .

# Run tests (requires Docker)
mvn test

# Skip integration tests
export SKIP_INTEGRATION_TESTS=true
mvn test

# Build the project
mvn clean package
```

## Architecture

The testcontainer consists of:

1. **Dockerfile**: Multi-stage build that clones a2a-java repo and builds the helloworld server
2. **A2AServerContainer**: Java wrapper that extends Testcontainers GenericContainer
3. **Test Classes**: Comprehensive tests and examples showing usage patterns

### Docker Image Details

- **Base Image**: Eclipse Temurin 17 JRE
- **Build Process**: Maven build of a2a-java helloworld server  
- **Port**: 9999 (configurable)
- **Health Check**: Quarkus health endpoint (`/q/health`)
- **User**: Non-root user for security

## A2A Server Details

The containerized server provides:

- **Agent Name**: "Hello World Agent"
- **Capabilities**: Streaming, push notifications, state transition history
- **Skills**: Single "hello_world" skill that returns "Hello World" responses
- **Protocol Version**: A2A 0.3.0
- **Endpoints**:
  - `/a2a/agent-card/public` - Get public agent card
  - `/a2a/invoke` - Send JSON-RPC messages
  - `/q/health` - Health check

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the same terms as the A2A Java project.

## Related Projects

- [A2A Java SDK](https://github.com/a2aproject/a2a-java) - The original A2A Java implementation
- [Testcontainers](https://www.testcontainers.org/) - Integration testing with Docker containers
