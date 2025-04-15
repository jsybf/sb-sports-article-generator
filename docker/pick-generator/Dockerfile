# 소스코드가 변해도 의존성은 케시가 되도록 의존성을 미리 다운로드
FROM maven:3.9-eclipse-temurin-21 AS dependencies

WORKDIR /opt/app

COPY llm-article-writer/cli/pom.xml llm-article-writer/cli/pom.xml
COPY llm-article-writer/database/pom.xml llm-article-writer/database/pom.xml
COPY llm-article-writer/llm-writer/pom.xml llm-article-writer/llm-writer/pom.xml
COPY llm-article-writer/scraper/pom.xml llm-article-writer/scraper/pom.xml
COPY pom.xml pom.xml

RUN mvn -B -e dependency:go-offline

FROM maven:3.9-eclipse-temurin-21 AS builder

# jar 빌드
WORKDIR /opt/app

COPY --from=dependencies /root/.m2 /root/.m2
COPY llm-article-writer  llm-article-writer
COPY pom.xml pom.xml

RUN mvn -B -e clean install -DskipTests

FROM eclipse-temurin:21 AS app

WORKDIR /opt/app
COPY --from=builder /opt/app/llm-article-writer/cli/target/cli-0.0.1.jar cli-0.0.1.jar

# playwright의 의존성들 다운로드
RUN java -jar cli-0.0.1.jar install-browser
# 아래의 환경변수를 세팅안해주면 firfox, webkit을 컨테이너를 실행할따마다 다운로드 받음
ENV PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=1

ENTRYPOINT java -jar cli-0.0.1.jar scrape --db /opt/app/db/sqlite.db --all && java -jar cli-0.0.1.jar generate --db /opt/app/db/sqlite.db 
