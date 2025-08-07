
# Build stage
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY backend/pom.xml .
RUN mvn -q -B -DskipTests package || true
COPY backend/. .
RUN mvn -q -B -DskipTests clean package

# Runtime stage
FROM eclipse-temurin:21-jre
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75"
WORKDIR /opt/app
COPY --from=build /app/target/mortgage-service.jar app.jar
EXPOSE 8080
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar app.jar"]