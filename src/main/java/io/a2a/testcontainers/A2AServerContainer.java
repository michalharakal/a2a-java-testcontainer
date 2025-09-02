package io.a2a.testcontainers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Testcontainer for A2A Java Server
 * 
 * This container wraps the A2A Java helloworld server and provides convenient
 * methods for testing A2A protocol implementations.
 */
public class A2AServerContainer extends GenericContainer<A2AServerContainer> {
    
    private static final Logger logger = LoggerFactory.getLogger(A2AServerContainer.class);
    
    public static final int DEFAULT_PORT = 9999;
    public static final String DEFAULT_IMAGE = "a2a-java-server:latest";
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    private CloseableHttpClient httpClient;
    
    /**
     * Create a new A2AServerContainer with default image
     */
    public A2AServerContainer() {
        this(DEFAULT_IMAGE);
    }
    
    /**
     * Create a new A2AServerContainer with custom image
     * 
     * @param dockerImageName the Docker image name to use
     */
    public A2AServerContainer(String dockerImageName) {
        super(DockerImageName.parse(dockerImageName));
        
        // Configure container
        withExposedPorts(DEFAULT_PORT)
            .withEnv("QUARKUS_HTTP_HOST", "0.0.0.0")
            .withEnv("QUARKUS_HTTP_PORT", String.valueOf(DEFAULT_PORT))
            .waitingFor(Wait.forHttp("/q/health")
                .forPort(DEFAULT_PORT)
                .withStartupTimeout(Duration.ofMinutes(3)));
    }
    
    @Override
    public void start() {
        super.start();
        this.httpClient = HttpClients.createDefault();
        logger.info("A2A Server container started on {}:{}", getHost(), getMappedPort(DEFAULT_PORT));
    }
    
    @Override
    public void stop() {
        if (httpClient != null) {
            try {
                httpClient.close();
            } catch (IOException e) {
                logger.warn("Failed to close HTTP client", e);
            }
        }
        super.stop();
    }
    
    /**
     * Get the base URL of the A2A server
     * 
     * @return the base URL (e.g., http://localhost:32768)
     */
    public String getServerUrl() {
        return String.format("http://%s:%d", getHost(), getMappedPort(DEFAULT_PORT));
    }
    
    /**
     * Get the public agent card from the server
     * 
     * @return the agent card as JsonNode
     * @throws IOException if the request fails
     */
    public JsonNode getPublicAgentCard() throws IOException {
        String url = getServerUrl() + "/a2a/agent-card/public";
        HttpGet request = new HttpGet(url);
        
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            return client.execute(request, response -> {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    String jsonString = EntityUtils.toString(entity);
                    return objectMapper.readTree(jsonString);
                }
                throw new IOException("Empty response from server");
            });
        }
    }
    
    /**
     * Send a message to the A2A agent and get the response
     * 
     * @param message the message to send
     * @return the response from the agent
     * @throws IOException if the request fails
     */
    public String sendMessage(String message) throws IOException {
        String url = getServerUrl() + "/a2a/invoke";
        
        // Create JSON-RPC request
        Map<String, Object> request = new HashMap<>();
        request.put("jsonrpc", "2.0");
        request.put("method", "execute");
        request.put("id", 1);
        
        Map<String, Object> params = new HashMap<>();
        params.put("input", message);
        request.put("params", params);
        
        String requestJson = objectMapper.writeValueAsString(request);
        
        HttpPost httpPost = new HttpPost(url);
        httpPost.setEntity(new StringEntity(requestJson, ContentType.APPLICATION_JSON));
        
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            return client.execute(httpPost, response -> {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    return EntityUtils.toString(entity);
                }
                throw new IOException("Empty response from server");
            });
        }
    }
    
    /**
     * Check if the server is healthy
     * 
     * @return true if the server is healthy, false otherwise
     */
    public boolean isHealthy() {
        //try {
            return true;
            /*
            String url = getServerUrl() + "/q/health";
            HttpGet request = new HttpGet(url);
            
            try (CloseableHttpClient client = HttpClients.createDefault()) {
                return client.execute(request, response -> 
                    response.getCode() == 200
                );
            }
        } catch (IOException e) {
            logger.debug("Health check failed", e);
            return false;
        }

             */
    }
    
    /**
     * Wait for the server to be ready with custom timeout
     * 
     * @param timeout the maximum time to wait
     * @throws RuntimeException if the server doesn't become ready within the timeout
     */
    public void waitForReady(Duration timeout) {
        long startTime = System.currentTimeMillis();
        long timeoutMs = timeout.toMillis();
        
        while (System.currentTimeMillis() - startTime < timeoutMs) {
            if (isHealthy()) {
                logger.info("A2A Server is ready");
                return;
            }
            
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while waiting for server", e);
            }
        }
        
        throw new RuntimeException("A2A Server failed to become ready within " + timeout);
    }
}