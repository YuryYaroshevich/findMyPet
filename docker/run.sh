#!/usr/bin/env bash

java -jar \
  -Xms300m -Xmx300m \
  -XX:+UseContainerSupport \
  petfinder.jar

  #-Djava.rmi.server.hostname=0.0.0.0 \
  #-Dcom.sun.management.jmxremote \
  #-Dcom.sun.management.jmxremote=true \
  #-Dcom.sun.management.jmxremote.port=9010 \
  #-Dcom.sun.management.jmxremote.rmi.port=9010 \
  #-Dcom.sun.management.jmxremote.authenticate=false \
  #-Dcom.sun.management.jmxremote.ssl=false \




