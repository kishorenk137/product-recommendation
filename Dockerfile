# ==============================================================================
# Production Dockerfile for Personalized Customer Insights Web Application
# Multi-stage build using Eclipse Temurin Java 21 Alpine
# ==============================================================================

# Stage 1: Build the application using Maven
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /build

# Copy Maven Wrapper and POM file first for efficient layer caching
COPY pom.xml mvnw* ./
COPY .mvn .mvn

# Make wrapper script executable and download dependencies
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B || true

# Copy full application source
COPY src src

# Build production executable JAR (skipping unit tests for fast image builds)
RUN ./mvnw clean package -DskipTests

# Stage 2: Minimal, secure JRE runtime container
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create unprivileged system user for hardened security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy compiled JAR from build stage
COPY --from=builder /build/target/*.jar app.jar

# Set ownership to unprivileged user
RUN chown -R appuser:appgroup /app
USER appuser

# Expose standard Spring Boot port
EXPOSE 8080

# Environment variables
ENV PORT=8080 \
    SPRING_PROFILES_ACTIVE=mysql \
    JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC"

# Healthcheck
HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:8080/login || exit 1

# Launch application with memory constraints and dynamic port binding
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dserver.port=${PORT:-8080} -jar app.jar"]
