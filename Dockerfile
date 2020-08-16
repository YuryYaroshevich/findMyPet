FROM openjdk:11-jdk-slim

WORKDIR app

# https://devcenter.heroku.com/articles/exec#enabling-docker-support
ADD ./.profile.d /app/.profile.d
RUN rm /bin/sh && ln -s /bin/bash /bin/sh
RUN apt-get update && \
    apt-get upgrade -y && \
    apt-get install -y curl openssh-client openssh-server python

COPY docker/* /app/
RUN chmod 777 run.sh

EXPOSE 8080
# jmx
EXPOSE 1098
EXPOSE 1099
EXPOSE 9010

CMD ["./run.sh"]

COPY build/libs/petfinder-*-SNAPSHOT.jar /app/petfinder.jar
