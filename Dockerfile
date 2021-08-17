FROM openjdk:15 AS builder
WORKDIR /app/
COPY . .
RUN ./mvnw clean package

FROM openjdk:15
COPY --from=builder /app/target/*.jar /app/
WORKDIR /app/
EXPOSE 8080
CMD ["java", "-jar", "reactive-web-1.0.0-SNAPSHOT.jar"]
