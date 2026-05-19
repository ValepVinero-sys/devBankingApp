# 1. Base image with JDK 25
FROM openjdk:25-jdk-slim

#2. Information about the maintainer
LABEL maintainer="Valep Vinreo"
LABEL description="Banking Application"

# 3. Working directory inside the container
WORKDIR /app

# 4. Copy the JAR file from the built project
COPY target/*.jar app.jar

# 5. Open the port on which the application runs
EXPOSE 8080

# 6. Entry point - a command to launch the application
ENTRYPOINT ["java", "-jar", "app.jar"]
