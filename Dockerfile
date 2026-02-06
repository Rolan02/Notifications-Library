# Stage 1: Build
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app

# Copy pom.xml first for dependency caching
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the project
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy the built jar from builder stage
COPY --from=builder /app/target/notification-lib-*.jar app.jar

# Copy examples
COPY --from=builder /app/target/classes/com/company/notifications/examples examples/

# Set environment variables
ENV JAVA_OPTS="-Xmx256m -Xms128m"

# Labels
LABEL maintainer="your-email@company.com"
LABEL version="1.0.0"
LABEL description="Notification Library - Multi-channel notification system"

# Create non-root user
RUN addgroup -g 1001 appuser && \
    adduser -D -u 1001 -G appuser appuser && \
    chown -R appuser:appuser /app

USER appuser

# Default command: run comprehensive example
CMD ["sh", "-c", "java ${JAVA_OPTS} -cp app.jar com.company.notifications.examples.ComprehensiveExample"]

# Alternative: Interactive shell
# ENTRYPOINT ["sh"]"-b"]