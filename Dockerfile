FROM openjdk:17-jdk-alpine

WORKDIR /app

COPY build/libs/smart-trader-0.0.1-SNAPSHOT.jar smarttrader-app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "smarttrader-app.jar"]
