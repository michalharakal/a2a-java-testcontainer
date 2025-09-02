# Multi-stage Dockerfile for A2A Java Server
FROM maven:3.9-eclipse-temurin-17 AS builder

# Set working directory
WORKDIR /app

# Clone the a2a-java repository
RUN git clone https://github.com/a2aproject/a2a-java.git .

# Build the entire project first to ensure dependencies are available
RUN mvn clean install -DskipTests

# Build the helloworld server specifically
WORKDIR /app/examples/helloworld/server
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre-jammy

# Set working directory
WORKDIR /app

# Copy the built application
COPY --from=builder /app/examples/helloworld/server/target/quarkus-app/ ./

# Create a non-root user
RUN groupadd -r a2a && useradd -r -g a2a a2a
RUN chown -R a2a:a2a /app
USER a2a

# Expose the default port
EXPOSE 9999


# Set environment variables
ENV QUARKUS_HTTP_HOST=0.0.0.0
ENV QUARKUS_HTTP_PORT=9999

# Run the application
CMD ["java", "-jar", "quarkus-run.jar"]