# ---- Build stage ----
# Matches the JDK this project is verified to build/run under (JDK 21;
# newer JDKs have been observed to break Lombok annotation processing here).
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Cache dependencies in their own layer: only re-downloads when pom.xml
# actually changes, not on every source edit.
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B dependency:go-offline

COPY src ./src
RUN ./mvnw -B clean package -DskipTests

# ---- Runtime stage ----
FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app

RUN addgroup --system spring && adduser --system --ingroup spring spring
COPY --from=build /app/target/*.jar app.jar
USER spring

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
