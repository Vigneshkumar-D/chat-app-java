# Use Maven for the build stage
FROM maven:3.8.5-openjdk-17 AS build
COPY . /app
WORKDIR /app
RUN mvn clean package -DskipTests

# Use a slim JDK image for the final stage
FROM openjdk:17.0.1-jdk-slim
COPY --from=build /app/target/chat-app-0.0.1-SNAPSHOT.jar chat-app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "chat-app.jar"]
