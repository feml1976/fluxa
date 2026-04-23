# ── Etapa 1: Compilación ────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /build

# Descargar dependencias antes de copiar el código fuente
# → si pom.xml no cambia, Docker reutiliza esta capa del cache
COPY pom.xml .
RUN mvn dependency:go-offline -q

COPY src ./src
RUN mvn clean package -DskipTests -q

# ── Etapa 2: Runtime mínimo ─────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

# Usuario no-root para reducir superficie de ataque
RUN addgroup -S fluxa && adduser -S fluxa -G fluxa

COPY --from=build /build/target/*.jar app.jar
RUN chown fluxa:fluxa app.jar
USER fluxa

EXPOSE 8080

ENTRYPOINT ["java", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
