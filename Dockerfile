# 빌드 단계
FROM openjdk:17-jdk-alpine as builder

# 소스 코드 복사
WORKDIR /app
COPY . .

# 애플리케이션 빌드
RUN chmod +x gradlew
RUN ./gradlew clean build -x test
RUN mv build/libs/*.jar app.jar

# 패키징 단계
FROM openjdk:17-jdk-alpine

WORKDIR /app

# 빌드 단계에서 생성된 JAR 파일 복사
COPY --from=builder /app/app.jar ./app.jar
COPY --from=builder /app/application.yml ./application.yml

# 포트 열기
EXPOSE 8088

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "-Dspring.config.location=file:/application.yml", "app.jar"]
