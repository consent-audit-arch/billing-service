FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY tcc-security-starter /tmp/tcc-security-starter
RUN cd /tmp/tcc-security-starter && mvn install -DskipTests -B

COPY billing-service/pom.xml .
RUN mvn dependency:go-offline -B
COPY billing-service/src ./src
RUN mvn package -DskipTests -B

FROM eclipse-temurin:21-jre-jammy
RUN groupadd -r appuser && useradd -r -g appuser -m appuser
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
COPY billing-service/opa/policies /opa/policies
COPY billing-service/entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh
RUN chown appuser:appuser app.jar /entrypoint.sh
EXPOSE 8080
ENV JAVA_OPTS=
ENTRYPOINT ["/entrypoint.sh"]
