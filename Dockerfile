FROM maven:3.9.4-eclipse-temurin-21 AS builder
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

FROM openjdk:21
WORKDIR /app
COPY target/SkinsStorage-1.0-SNAPSHOT.jar /app/SkinsStorage.jar
EXPOSE 8989

CMD ["java", "-jar", "SkinsStorage.jar"]