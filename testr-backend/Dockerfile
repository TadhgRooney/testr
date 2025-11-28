# ===== STAGE 1: BUILD THE SPRING BOOT APP =====
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app

# Copy Maven config and source
COPY pom.xml .
COPY src ./src

# Build the jar, skipping tests for speed
RUN mvn -B -DskipTests clean package


# ===== STAGE 2: RUN THE BUILT APP =====
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copy jar from builder stage
COPY --from=builder /app/target/*.jar app.jar

# Render will set PORT. Default to 8080 if not set.
ENV PORT=8080
EXPOSE 8080

# Start Spring Boot on the PORT Render gives us
CMD ["sh", "-c", "java -jar app.jar --server.port=${PORT}"]
