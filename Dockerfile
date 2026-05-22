# ──────────────────────────────────────────────────────────────
# Backend Dockerfile — zwei Modi:
#
# MODUS A (empfohlen, kein Internet im Container nötig):
#   Baue das JAR zuerst lokal:
#     cd Backend\WebChain && mvnw.cmd package -DskipTests
#   Dann: docker-compose up --build
#   → dieses Dockerfile kopiert das fertige JAR aus ./target/
#
# MODUS B (vollständiger In-Container-Build, braucht DNS):
#   Tausche dieses Dockerfile gegen Dockerfile.full aus.
#   Vorher Docker Desktop neu starten nachdem daemon.json DNS gesetzt wurde.
# ──────────────────────────────────────────────────────────────

# ── Runtime Image (kein Build-Stage nötig) ───────────────────
FROM eclipse-temurin:17-jre-alpine

LABEL maintainer="BlockChain Backend"
LABEL description="Spring Boot Blockchain REST API"

# Non-root user
RUN addgroup -S blockchain && adduser -S blockchain -G blockchain

WORKDIR /app

# Copy the pre-built JAR from the local target/ directory.
# The *-plain.jar (plain archive without deps) is excluded via the glob.
# Build first with: mvnw.cmd package -DskipTests
COPY target/*.jar app.jar

RUN chown blockchain:blockchain app.jar

USER blockchain

EXPOSE 8080

# Health check (wget is available in eclipse-temurin:alpine; curl is not)
HEALTHCHECK --interval=20s --timeout=10s --start-period=40s --retries=5 \
    CMD wget -qO- http://localhost:8080/api/blockchain/status || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
