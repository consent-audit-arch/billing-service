#!/bin/sh
cp -r /opa/policies/. /shared-policies/
exec su -s /bin/sh appuser -c "exec /opt/java/openjdk/bin/java $JAVA_OPTS -jar /app/app.jar"
