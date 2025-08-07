FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

# Copy only pom.xml first to leverage Docker cache for deps
COPY backend/pom.xml .
RUN mvn -q -B dependency:go-offline

# Copy the rest of the source and build
COPY backend/. .
RUN mvn -q -B -DskipTests clean package

###############################
# 2️⃣ Runtime stage
###############################
FROM amazoncorretto:21.0.6

# ───────── Runtime tweaks ─────────
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75"
VOLUME /tmp
EXPOSE 8080            # change if your Spring server.port differs

# Create unprivileged user
RUN microdnf update -y \
 && microdnf install -y shadow-utils \
 && microdnf clean all \
 && groupadd --gid 5678 appgroup \
 && useradd  --uid 5678 --gid 5678 --no-create-home --shell /sbin/nologin appuser \
 && mkdir -p /app \
 && chown -R appuser:appgroup /app

WORKDIR /app

# Copy fat/uber JAR from build stage
COPY --from=build /workspace/target/*.jar app.jar

# (Optional) copy Application Insights or other agents
# COPY misc/azure/applicationinsights-agent-*.jar app-insights-agent.jar

# Switch to non-root user
USER appuser

# ───────── Entrypoint ─────────
ENTRYPOINT ["/bin/sh","-c","exec java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar app.jar"]