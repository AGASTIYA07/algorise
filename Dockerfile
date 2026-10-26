# Multi-stage Dockerfile for Stock Market Portfolio Tracker Web App
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app
COPY src/ ./src/
RUN mkdir -p bin && javac -d bin src/*.java
RUN jar cfe StockMarketWebServer.jar StockMarketWebServer -C bin .

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/StockMarketWebServer.jar .
COPY web/ ./web/
EXPOSE 8080
CMD ["sh", "-c", "java -jar StockMarketWebServer.jar ${PORT:-8080}"]
