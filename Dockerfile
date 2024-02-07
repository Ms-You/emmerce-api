# base-image
FROM openjdk:17-jdk-alpine

# application jar file and yml file
ARG JAR_FILE
ARG YML_FILE

# add jar file to container
COPY ${JAR_FILE} app.jar
# add application.yml file to container
COPY ${YML_FILE} /application.yml

# Install tzdata package and set TIMEZONE
RUN apk add --no-cache tzdata \
    && cp /usr/share/zoneinfo/Asia/Seoul /etc/localtime \
    && echo "Asia/Seoul" > /etc/timezone

# expose port
EXPOSE 8088

# run jar file
ENTRYPOINT ["java", "-jar", "-Dspring.config.location=file:/application.yml", "app.jar"]
