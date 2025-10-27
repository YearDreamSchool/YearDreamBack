FROM gradle:8.8-jdk21-jammy AS builder
WORKDIR /app

COPY gradlew ./
COPY gradle/ gradle/
COPY build.gradle settings.gradle ./

RUN chmod +x ./gradlew

RUN ./gradlew --no-daemon dependencies || true

COPY src/ src/
RUN ./gradlew bootJar -x test --no-daemon

FROM openjdk:21-jdk-slim
WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

COPY src/main/resources/bootsecurity.p12 ./bootsecurity.p12

ENV TZ=Asia/Seoul

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
