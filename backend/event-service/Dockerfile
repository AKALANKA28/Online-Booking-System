FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml ./
COPY api-gateway/pom.xml api-gateway/pom.xml
COPY event-service/pom.xml event-service/pom.xml
COPY seat-service/pom.xml seat-service/pom.xml
COPY booking-service/pom.xml booking-service/pom.xml
COPY payment-notification-service/pom.xml payment-notification-service/pom.xml
COPY user-service/pom.xml user-service/pom.xml
RUN mvn -q -pl event-service -am dependency:go-offline
COPY . .
RUN mvn -q -pl event-service -am clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/event-service/target/event-service-1.0.0.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java","-jar","app.jar"]
