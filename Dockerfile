FROM openjdk:11-jdk-slim

WORKDIR app

COPY docker/* /app/
RUN chmod 777 run.sh

EXPOSE 8080

CMD ["./run.sh"]

COPY build/libs/petfinder-*-SNAPSHOT.jar /app/petfinder.jar
