FROM maven:3.9.11-eclipse-temurin-25

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

EXPOSE 8080

CMD ["java", "-jar", "target/recipehub-0.0.1-SNAPSHOT.jar"]