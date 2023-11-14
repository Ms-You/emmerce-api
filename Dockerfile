# base-image
FROM openjdk:17-jdk-alpine

# application jar file
ENV JAR_FILE=./build/libs/emmerce-0.0.1-SNAPSHOT.jar

# add jar file to container
COPY ${JAR_FILE} app.jar

# expose port
EXPOSE 8088

# run jar file
ENTRYPOINT ["java", "-jar", "app.jar"]
