# Use the official Maven image as the build environment
FROM maven:3.9.5-eclipse-temurin-21 AS build

# Set the working directory in the Docker container
WORKDIR /app

# Copy the pom.xml file into our app directory
COPY app/pom.xml .

# Download all required dependencies into the image (this will cache dependencies between builds)
RUN mvn dependency:go-offline

# Now, copy the source code into the image
COPY app/src ./src

# Package our application. The resulting jar will be at /app/target/my-app.jar
RUN mvn clean package -DskipTests

# Now, use the OpenJDK 21 slim image for our run environment
FROM openjdk:21-slim

# Set the working directory
WORKDIR /app

ARG APP_PROFILE_ARG
ARG SERVER_PORT_ARG
ENV APP_PROFILE=${APP_PROFILE_ARG}
ENV SERVER_PORT=${SERVER_PORT_ARG}

# Copy the built jar file into our run environment
COPY --from=build /app/target/*.jar app.jar

# Expose the server port
EXPOSE $SERVER_PORT

# Set the command to run our application
ENTRYPOINT ["java", "-jar", "app.jar"]
