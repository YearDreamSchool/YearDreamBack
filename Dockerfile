FROM gradle:8.8-jdk21-jammy AS builder
WORKDIR /app

# Gradle Wrapper, 설정 파일, 빌드 스크립트 복사 (이후 캐싱을 위해)
COPY gradlew ./
COPY gradle/ gradle/
COPY build.gradle settings.gradle ./

# Gradle Wrapper 실행 권한 부여
RUN chmod +x ./gradlew

# 의존성만 미리 다운로드하여 캐시 레이어를 만듭니다. 
# 소스 코드가 변경되어도 이 레이어는 다시 실행되지 않아 빌드 속도를 높입니다.
# 의존성 다운로드 실패를 무시하고 다음 단계로 진행합니다.
RUN ./gradlew --no-daemon dependencies || true

# 소스 복사 및 JAR 파일 생성
# 'test' 태스크를 제외하고 최종 JAR 파일(bootJar)을 생성합니다.
COPY src/ src/
RUN ./gradlew bootJar -x test --no-daemon

# ==============================================================================
# 2. 실행 스테이지 (Runtime Stage)
# JAR 파일 실행에 필요한 최소한의 환경만 구성합니다.
# ==============================================================================
# openjdk:21-jdk-slim은 OS와 JDK만 포함하여 매우 가볍습니다.
FROM openjdk:21-jdk-slim
WORKDIR /app

# 빌드 스테이지에서 생성된 JAR 파일 복사
# 빌드 스테이지의 /app/build/libs/ 경로에서 .jar 파일을 찾아 현재 스테이지의 /app/app.jar로 복사합니다.
COPY --from=builder /app/build/libs/*.jar app.jar

# 시간대 설정 (선택 사항이지만 권장)
ENV TZ=Asia/Seoul
# Spring Boot 앱이 사용할 포트
EXPOSE 8080

# Spring Boot 앱 실행 명령어
ENTRYPOINT ["java", "-jar", "/app/app.jar"]