# 소스코드가 변해도 의존성은 케시가 되도록 의존성을 미리 다운로드
FROM maven:3.9-eclipse-temurin-21 AS dependencies

WORKDIR /opt/app

COPY pick-generator/scraper/baseball-scraper pick-generator/scraper/baseball-scraper
COPY pick-generator/scraper/basketball-scraper pick-generator/scraper/basketball-scraper
COPY pick-generator/scraper/hockey-scraper pick-generator/scraper/hockey-scraper
COPY pick-generator/scraper/database pick-generator/scraper/database
COPY pick-generator/cli pick-generator/cli
COPY pick-generator/claude pick-generator/claude
COPY download-server download-server

COPY pom.xml pom.xml

RUN mvn -B -e dependency:go-offline

FROM maven:3.9-eclipse-temurin-21 AS builder

# jar 빌드
WORKDIR /opt/app

COPY --from=dependencies /root/.m2 /root/.m2
COPY pick-generator  pick-generator
COPY download-server  download-server
COPY pom.xml pom.xml

RUN mvn -B -e -T 1C  clean package -pl ./pick-generator/cli -am -DskipTests

FROM eclipse-temurin:21 AS app
ARG cliJar=cli-0.0.1-jar-with-.jar
WORKDIR /
COPY --from=builder /opt/app/pick-generator/cli/target/cli-fatjar-0.0.1.jar cli-fatjar-0.0.1.jar

# playwright의 의존성들 다운로드
RUN java -jar ${cliJar} install-browser
# 아래의 환경변수를 세팅안해주면 firfox, webkit을 컨테이너를 실행할따마다 다운로드 받음
ENV PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=1

CMD java -jar cli-0.0.1.jar scrape --db /opt/app/db/sqlite.db --all && java -jar cli-0.0.1.jar generate --db /opt/app/db/sqlite.db 
