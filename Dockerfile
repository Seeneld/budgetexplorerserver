FROM maven:3.8.4-openjdk-11-slim AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

FROM openjdk:11-jre-slim
WORKDIR /app
COPY --from=build /app/target/databasesync-1.0-SNAPSHOT.jar app.jar
RUN mkdir -p /app/database
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"] 