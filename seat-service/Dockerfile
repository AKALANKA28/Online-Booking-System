FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml ./
COPY api-gateway/pom.xml api-gateway/pom.xml
COPY event-service/pom.xml event-service/pom.xml
COPY seat-service/pom.xml seat-service/pom.xml
COPY booking-service/pom.xml booking-service/pom.xml
COPY payment-notification-service/pom.xml payment-notification-service/pom.xml
RUN mvn -q -pl seat-service -am dependency:go-offline
COPY . .
RUN mvn -q -pl seat-service -am clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/seat-service/target/seat-service-1.0.0.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java","-jar","app.jar"]
