FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/SkinsStorage-1.0-SNAPSHOT.jar /app/SkinsStorage.jar

EXPOSE 8989

CMD ["java", "-jar", "SkinsStorage.jar"]