# 소스코드가 변해도 의존성은 케시가 되도록 의존성을 미리 다운로드
FROM maven:3.9-eclipse-temurin-21 AS dependencies

WORKDIR /opt/app

COPY download-server/pom.xml download-server/pom.xml
COPY llm-article-writer/database/pom.xml llm-article-writer/database/pom.xml
COPY llm-article-writer/cli/pom.xml llm-article-writer/cli/pom.xml
COPY llm-article-writer/llm-writer/pom.xml llm-article-writer/llm-writer/pom.xml
COPY llm-article-writer/scraper/pom.xml llm-article-writer/scraper/pom.xml
COPY pom.xml pom.xml

RUN mvn -B -e dependency:go-offline

# build jar
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /opt/app

COPY --from=dependencies /root/.m2 /root/.m2
COPY download-server download-server
COPY llm-article-writer llm-article-writer
COPY pom.xml pom.xml

RUN mvn -B -e clean package -DskipTests

FROM eclipse-temurin:21 AS app

WORKDIR /opt/app
COPY --from=builder /opt/app/download-server/target/donwload-server-0.0.1-jar-with-dependencies.jar download-server-0.0.1.jar

ENTRYPOINT java -jar download-server-0.0.1.jar
