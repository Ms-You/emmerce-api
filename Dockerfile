# base-image
FROM openjdk:17-jdk-alpine

# application jar file and yml file
ARG JAR_FILE
ARG YML_FILE

# add jar file to container
COPY ${JAR_FILE} app.jar
# add application.yml file to container
COPY ${YML_FILE} /app/application.yml

# expose port
EXPOSE 8088

# run jar file
ENTRYPOINT ["java", "-jar", "app.jar"]
