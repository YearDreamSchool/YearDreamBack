# 1. 빌드 스테이지 (Build Stage) - 컴파일 및 JAR 생성
FROM gradle:8.8-jdk21-jammy AS builder
WORKDIR /app

# Gradle Wrapper 및 설정 복사 (의존성 캐싱 목적)
COPY gradlew ./
COPY gradle/ gradle/
COPY build.gradle settings.gradle ./

# Ensure wrapper is executable and populate Gradle cache (다운로드만 해서 캐시)
RUN chmod +x ./gradlew
# 'dependencies'로 의존성만 미리 내려받아 캐시합니다. 실패해도(환경에 따라) 이후 빌드에서 다시 시도됩니다.
RUN ./gradlew --no-daemon dependencies || true

# 소스 복사 및 JAR 생성
COPY src/ src/
RUN ./gradlew bootJar -x test --no-daemon

# 2. 실행 스테이지 (Runtime Stage) - 가볍게 실행
FROM openjdk:21-jdk-slim
WORKDIR /app

# 빌드 스테이지에서 생성된 JAR 파일 복사 (와일드카드로 안전하게 복사)
COPY --from=builder /app/build/libs/*.jar app.jar

# Spring Boot 앱 실행
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

# 앱이 사용할 포트
EXPOSE 8080