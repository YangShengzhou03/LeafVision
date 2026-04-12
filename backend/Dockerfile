FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN apk add --no-cache maven && \
    mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

RUN apk add --no-cache curl

COPY --from=builder /app/target/*.jar app.jar

ENV JAVA_OPTS="-Xms256m -Xmx512m"
ENV SERVER_PORT=8081

EXPOSE 8081

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8081/api/dashboard/overview || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
