package io.a2a.testcontainers;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;

import java.io.IOException;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for A2AServerContainer
 * 
 * Note: These tests require Docker to be running and will build/pull the A2A server image.
 * Set SKIP_INTEGRATION_TESTS=true to skip these tests.
 */
@DisabledIfEnvironmentVariable(named = "SKIP_INTEGRATION_TESTS", matches = "true")
class A2AServerContainerTest {
    
    private A2AServerContainer container;
    
    @BeforeEach
    void setUp() {
        // Note: You'll need to build the Docker image first:
        // docker build -t a2a-java-server:latest .
        container = new A2AServerContainer();
    }
    
    @AfterEach
    void tearDown() {
        if (container != null) {
            container.stop();
        }
    }
    
    @Test
    void testContainerStartsAndBecomesHealthy() {
        // Start the container
        container.start();
        
        // Verify container is running
        assertTrue(container.isRunning(), "Container should be running");
        
        // Verify health check passes
        assertTrue(container.isHealthy(), "Container should be healthy");
        
        // Verify server URL is accessible
        assertNotNull(container.getServerUrl(), "Server URL should not be null");
        assertTrue(container.getServerUrl().startsWith("http://"), 
                   "Server URL should start with http://");
    }
    
    @Test
    void testGetPublicAgentCard() throws IOException {
        container.start();
        
        // Get the public agent card
        JsonNode agentCard = container.getPublicAgentCard();
        
        // Verify agent card structure
        assertNotNull(agentCard, "Agent card should not be null");
        assertTrue(agentCard.has("name"), "Agent card should have a name");
        assertTrue(agentCard.has("description"), "Agent card should have a description");
        assertTrue(agentCard.has("version"), "Agent card should have a version");
        assertTrue(agentCard.has("capabilities"), "Agent card should have capabilities");
        assertTrue(agentCard.has("skills"), "Agent card should have skills");
        
        // Verify specific values
        assertEquals("Hello World Agent", agentCard.get("name").asText());
        assertEquals("Just a hello world agent", agentCard.get("description").asText());
        assertEquals("1.0.0", agentCard.get("version").asText());
        assertEquals("0.3.0", agentCard.get("protocolVersion").asText());
        
        // Verify capabilities
        JsonNode capabilities = agentCard.get("capabilities");
        assertTrue(capabilities.get("streaming").asBoolean());
        assertTrue(capabilities.get("pushNotifications").asBoolean());
        assertTrue(capabilities.get("stateTransitionHistory").asBoolean());
        
        // Verify skills
        JsonNode skills = agentCard.get("skills");
        assertTrue(skills.isArray());
        assertEquals(1, skills.size());
        
        JsonNode skill = skills.get(0);
        assertEquals("hello_world", skill.get("id").asText());
        assertEquals("Returns hello world", skill.get("name").asText());
        assertEquals("just returns hello world", skill.get("description").asText());
    }
    
    @Test
    void testSendMessage() throws IOException {
        container.start();
        
        // Send a message to the agent
        String response = container.sendMessage("Hello, how are you?");
        
        // Verify response is not null or empty
        assertNotNull(response, "Response should not be null");
        assertFalse(response.trim().isEmpty(), "Response should not be empty");
        
        // The response should be a JSON-RPC response
        // containing "Hello World" in some form
        assertTrue(response.contains("Hello World") || response.contains("hello world"), 
                   "Response should contain 'Hello World'");
    }
    
    @Test
    void testWaitForReady() {
        container.start();
        
        // This should complete quickly since the container is already started
        assertDoesNotThrow(() -> {
            container.waitForReady(Duration.ofSeconds(10));
        }, "waitForReady should not throw an exception for a running container");
    }
    
    @Test
    void testWaitForReadyTimeout() {
        // Don't start the container to test timeout
        assertThrows(RuntimeException.class, () -> {
            container.waitForReady(Duration.ofSeconds(1));
        }, "waitForReady should throw RuntimeException when timeout is reached");
    }
    
    @Test
    void testCustomImage() {
        // Test with custom image name
        A2AServerContainer customContainer = new A2AServerContainer("a2a-java-server:custom");
        
        // Start and verify it works
        customContainer.start();
        try {
            assertTrue(customContainer.isRunning(), "Custom container should be running");
            assertTrue(customContainer.isHealthy(), "Custom container should be healthy");
        } finally {
            customContainer.stop();
        }
    }
    
    @Test
    void testMultipleContainers() {
        // Test running multiple containers simultaneously
        A2AServerContainer container1 = new A2AServerContainer();
        A2AServerContainer container2 = new A2AServerContainer();
        
        try {
            container1.start();
            container2.start();
            
            // Both should be running on different ports
            assertTrue(container1.isRunning(), "Container 1 should be running");
            assertTrue(container2.isRunning(), "Container 2 should be running");
            
            assertNotEquals(container1.getMappedPort(A2AServerContainer.DEFAULT_PORT),
                           container2.getMappedPort(A2AServerContainer.DEFAULT_PORT),
                           "Containers should be running on different ports");
            
            // Both should be healthy
            assertTrue(container1.isHealthy(), "Container 1 should be healthy");
            assertTrue(container2.isHealthy(), "Container 2 should be healthy");
            
        } finally {
            container1.stop();
            container2.stop();
        }
    }
}