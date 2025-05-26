# 소스코드가 변해도 의존성은 케시가 되도록 의존성을 미리 다운로드
FROM maven:3.9-eclipse-temurin-21 AS dependencies

WORKDIR /opt/app

COPY pick-generator/scraper/scraper-base/pom.xml pick-generator/scraper/scraper-base/pom.xml
COPY pick-generator/scraper/baseball-scraper/pom.xml pick-generator/scraper/baseball-scraper/pom.xml
COPY pick-generator/scraper/basketball-scraper/pom.xml pick-generator/scraper/basketball-scraper/pom.xml
COPY pick-generator/scraper/hockey-scraper/pom.xml pick-generator/scraper/hockey-scraper/pom.xml
COPY pick-generator/scraper/soccer-minor-scraper/pom.xml pick-generator/scraper/soccer-minor-scraper/pom.xml
COPY pick-generator/scraper/k1-scraper/pom.xml pick-generator/scraper/k1-scraper/pom.xml
COPY pick-generator/database/pom.xml pick-generator/database/pom.xml
COPY pick-generator/cli/pom.xml pick-generator/cli/pom.xml
COPY pick-generator/claude/pom.xml pick-generator/claude/pom.xml
COPY download-server/pom.xml download-server/pom.xml
COPY pom.xml pom.xml

RUN mvn -B -e dependency:go-offline

FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /opt/app

COPY --from=dependencies /root/.m2 /root/.m2
COPY download-server download-server
COPY pick-generator  pick-generator
COPY pom.xml pom.xml

RUN mvn -B -e -T 1C clean package -pl ./download-server -am -DskipTests

FROM eclipse-temurin:21 AS app

WORKDIR /opt/app
ARG app_version
COPY --from=builder /opt/app/download-server/target/donwload-server-${app_version}-jar-with-dependencies.jar download-server.jar

ENTRYPOINT java -Duser.timezone=Asia/Seoul -jar download-server.jar
