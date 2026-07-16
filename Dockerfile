FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Instala Maven (imagen alpine no lo trae por defecto)
RUN apk add --no-cache maven

# Copiar solo el pom primero para aprovechar la cache de capas de Docker
COPY pom.xml .
RUN mvn -B dependency:go-offline

# Copiar el resto del codigo y compilar (jar con dependencias incluidas)
COPY src ./src
RUN mvn -B clean package -DskipTests

# ---- Imagen final, mas liviana, solo con el JRE ----
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=build /app/target/notifications-lib-1.0.0.jar ./notifications-lib.jar

CMD ["java", "-jar", "notifications-lib.jar"]
