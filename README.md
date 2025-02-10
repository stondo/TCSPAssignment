# TCSP Assignment

This project compares two JSON trees—an initial tree and a new tree—to generate a merged "order" tree. For each element, an operation (CREATE, UPDATE, NO_ACTION, or DELETE) is computed based on the differences between the trees. The project is implemented in Java 21 using Jackson for JSON processing, SLF4J with Logback for logging, and Maven for build automation. A Dockerfile is provided for containerized execution.

## Project Structure
```
TCSPAssignment/
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com
│   │   │       └── swisscom
│   │   │           └── tcsp
│   │   │               ├── model
│   │   │               │   ├── Attribute.java
│   │   │               │   └── TreeItem.java
│   │   │               ├── service
│   │   │               │   └── TreeComparator.java
│   │   │               ├── Main.java
│   │   └── resources
│   │       ├── initial_tree.json
│   │       ├── logback.xml
│   │       └── new_tree.json
│   └── test
│       ├── java
│       │   └── com
│       │       └── swisscom
│       │           └── tcsp
│       │               └── service
│       │                   └── TreeComparatorTest.java
│       └── resources
│           ├── initial_tree.json
│           ├── new_tree.json
│           └── order_tree.json
├── .gitignore
├── dependency-reduced-pom.xml
├── Dockerfile
├── pom.xml
├── README.md
```


## Prerequisites

- **Java:** JDK 21
- **Maven:** 3.x

## Building the Project

Use Maven to compile and package the project. The Maven Shade Plugin is configured to produce an executable "fat" JAR with all dependencies included.

```bash
  mvn clean package
```

After a successful build, the shaded JAR will be located in the target directory and typically named: TCSPAssignment-1.0-SNAPSHOT.jar

### Running the Application
You can run the application directly from the command line:

```bash
  java -jar target/TCSPAssignment-1.0-SNAPSHOT.jar
```

The application will read the JSON files (initial_tree.json and new_tree.json) from the classpath and write the merged output as order.json to the project root directory.


## Running with Docker
A Dockerfile is provided to build and run the application in a container. The Dockerfile will print the generated order.json to the STDOUT.

### Build the Docker Image
```bash
  docker build -t tcsp-app .
```

### Run the Docker Container
A Dockerfile is provided to build and run the application in a container.
```bash
  docker run --rm tcsp-app
```

### Running Tests
JUnit 5 tests are included to verify the functionality of the tree comparison logic. To run tests:

```bash
  mvn test
```

### Logging
The project uses SLF4J with Logback for logging. The logging configuration is defined in src/main/resources/logback.xml.







