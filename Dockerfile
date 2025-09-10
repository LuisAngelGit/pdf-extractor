# ---------- build ----------
FROM maven:3.9.6-eclipse-temurin-11 AS build
ENV LANG=C.UTF-8 LC_ALL=C.UTF-8
WORKDIR /workspace
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline
COPY src ./src
ENV MAVEN_OPTS="-Dfile.encoding=UTF-8"
RUN mvn -e -X -DskipTests -Dmaven.test.skip=true resources:resources
RUN mvn -q -DskipTests -Dmaven.test.skip=true package

# ---------- runtime ----------
FROM eclipse-temurin:11-jre
ENV LANG=C.UTF-8 LC_ALL=C.UTF-8 JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF-8"
WORKDIR /app
COPY --from=build /workspace/target/*.jar app.jar
CMD ["sh","-c","java -Dserver.port=${PORT} -jar app.jar"]
