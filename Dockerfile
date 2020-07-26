FROM openjdk:11-jdk-slim

WORKDIR app

EXPOSE 8080

CMD ["java", "-jar", "petfinder.jar"]

COPY build/libs/petfinder-*-SNAPSHOT.jar /app/petfinder.jar


