#!/usr/bin/env bash

java -jar \
  -Xms300m -Xmx300m \
  -XX:+UseContainerSupport \
  -Dcom.sun.management.jmxremote \
  -Dcom.sun.management.jmxremote.port=1098 \
  -Dcom.sun.management.jmxremote.authenticate=false \
  -Dcom.sun.management.jmxremote.ssl=false \
  petfinder.jar
