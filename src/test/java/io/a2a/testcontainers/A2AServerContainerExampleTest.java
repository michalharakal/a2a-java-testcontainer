package io.a2a.testcontainers;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;

import java.io.IOException;
import java.time.Duration;

/**
 * Simple example showing how to use A2AServerContainer in your tests
 * 
 * This example demonstrates the basic usage pattern for the A2A Server testcontainer.
 */
@DisabledIfEnvironmentVariable(named = "SKIP_INTEGRATION_TESTS", matches = "true")
public class A2AServerContainerExampleTest {

    @Test
    void simpleUsageExample() throws IOException {
        // Step 1: Create and start the container
        try (A2AServerContainer a2aServer = new A2AServerContainer()) {
            a2aServer.start();
            
            // Step 2: Wait for the server to be ready (optional - start() already waits)
            a2aServer.waitForReady(Duration.ofMinutes(1));
            
            // Step 3: Get the server URL for your client
            String serverUrl = a2aServer.getServerUrl();
            System.out.println("A2A Server is running at: " + serverUrl);
            
            // Step 4: Interact with the server
            
            // Get the agent's public card
            JsonNode agentCard = a2aServer.getPublicAgentCard();
            System.out.println("Agent name: " + agentCard.get("name").asText());
            System.out.println("Agent description: " + agentCard.get("description").asText());
            
            // Send a message to the agent
            String response = a2aServer.sendMessage("Hi there!");
            System.out.println("Agent response: " + response);
            
            // Check if the server is healthy
            boolean isHealthy = a2aServer.isHealthy();
            System.out.println("Server is healthy: " + isHealthy);
            
            // Step 5: Use the serverUrl with your A2A client for testing
            // For example, if you have an A2A client:
            // A2AClient client = new A2AClient(serverUrl);
            // client.connect();
            // String result = client.sendMessage("test message");
            // assertEquals("Hello World", result);
            
        } // Container automatically stops when closed (try-with-resources)
    }
    
    @Test
    void advancedUsageExample() throws IOException {
        // Create container with custom configuration
        A2AServerContainer container = new A2AServerContainer()
            .withEnv("CUSTOM_SETTING", "value") // Add custom environment variables
            .withLogConsumer(outputFrame -> {
                // Optional: capture container logs for debugging
                System.out.print("[A2A-SERVER] " + outputFrame.getUtf8String());
            });
        
        try {
            container.start();
            
            // Your test logic here
            JsonNode agentCard = container.getPublicAgentCard();
            
            // Verify agent capabilities
            JsonNode capabilities = agentCard.get("capabilities");
            assert capabilities.get("streaming").asBoolean() : "Should support streaming";
            assert capabilities.get("pushNotifications").asBoolean() : "Should support push notifications";
            
            // Test specific skills
            JsonNode skills = agentCard.get("skills");
            assert skills.isArray() : "Skills should be an array";
            assert skills.size() == 1 : "Should have exactly one skill";
            
            JsonNode skill = skills.get(0);
            assert "hello_world".equals(skill.get("id").asText()) : "Skill ID should be hello_world";
            
        } finally {
            container.stop();
        }
    }
    
    /**
     * Example of using the container in a real test scenario
     */
    @Test 
    void realWorldTestScenario() throws IOException {
        try (A2AServerContainer a2aServer = new A2AServerContainer()) {
            a2aServer.start();
            
            // Simulate a real testing scenario where you want to:
            // 1. Start an A2A server
            // 2. Connect your client to it
            // 3. Test various interactions
            
            String serverUrl = a2aServer.getServerUrl();
            
            // Test 1: Verify server is accessible
            assert a2aServer.isHealthy() : "Server should be healthy";
            
            // Test 2: Verify agent card is correct
            JsonNode agentCard = a2aServer.getPublicAgentCard();
            assert "Hello World Agent".equals(agentCard.get("name").asText()) : "Agent name mismatch";
            
            // Test 3: Test message handling
            String response = a2aServer.sendMessage("test input");
            assert response != null : "Response should not be null";
            assert response.contains("Hello World") : "Response should contain expected text";
            
            // Test 4: Test with different input
            String response2 = a2aServer.sendMessage("another test");
            assert response2 != null : "Second response should not be null";
            
            // At this point, you would typically create your actual A2A client
            // and perform more sophisticated tests using the serverUrl
            
            System.out.println("✓ All tests passed! A2A Server testcontainer is working correctly.");
        }
    }
}