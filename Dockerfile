# --- Build stage: compile and package the jar ---
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app

# Cache dependencies first
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

# Build (tests need a Docker socket via Testcontainers, so skip them in the image build)
COPY src ./src
RUN mvn -B -q clean package -DskipTests

# --- Runtime stage: slim JRE with just the jar ---
FROM eclipse-temurin:21-jre
WORKDIR /app

RUN groupadd --system mauripay && useradd --system --gid mauripay mauripay

COPY --from=builder /app/target/*.jar app.jar
RUN chown -R mauripay:mauripay /app
USER mauripay

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "-Xms256m", "-Xmx512m", "app.jar"]
