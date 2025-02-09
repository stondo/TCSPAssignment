# Stage 1: Build the application with Maven using Java 21
FROM maven:3.9.9-amazoncorretto-21-alpine AS build
WORKDIR /app

# Copy pom.xml and source code into the container
COPY pom.xml .
COPY src ./src

# Build the project and create the shaded (uber) JAR (skipping tests if desired)
RUN mvn clean package -DskipTests

# Stage 2: Create the runtime image using Java 21
FROM amazoncorretto:21
WORKDIR /app

# Copy the shaded JAR from the build stage into the runtime image.
# Adjust the JAR name if different.
COPY --from=build /app/target/TCSPAssignment-1.0-SNAPSHOT.jar app.jar

# Specify the command to run the application
ENTRYPOINT ["sh", "-c", "java -jar app.jar && cat /app/order.json"]


