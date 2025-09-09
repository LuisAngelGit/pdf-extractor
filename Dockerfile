# ---------- build ----------
FROM maven:3.9.6-eclipse-temurin-11 AS build
WORKDIR /workspace
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline
COPY src ./src
RUN mvn -q -DskipTests -Dmaven.test.skip=true package

# ---------- runtime ----------
FROM eclipse-temurin:11-jre
WORKDIR /app
COPY --from=build /workspace/target/*.jar app.jar
CMD ["sh","-c","java -Dserver.port=${PORT} -jar app.jar"]
