# 소스코드가 변해도 의존성은 케시가 되도록 의존성을 미리 다운로드
FROM maven:3.9-eclipse-temurin-21 AS dependencies
WORKDIR /opt/app
COPY pick-generator/scraper/scraper-base/pom.xml pick-generator/scraper/scraper-base/pom.xml
COPY pick-generator/scraper/baseball-scraper/pom.xml pick-generator/scraper/baseball-scraper/pom.xml
COPY pick-generator/scraper/basketball-scraper/pom.xml pick-generator/scraper/basketball-scraper/pom.xml
COPY pick-generator/scraper/hockey-scraper/pom.xml pick-generator/scraper/hockey-scraper/pom.xml
COPY pick-generator/scraper/soccer-minor-scraper/pom.xml pick-generator/scraper/soccer-minor-scraper/pom.xml
COPY pick-generator/database/pom.xml pick-generator/database/pom.xml
COPY pick-generator/cli/pom.xml pick-generator/cli/pom.xml
COPY pick-generator/claude/pom.xml pick-generator/claude/pom.xml
COPY download-server/pom.xml download-server/pom.xml
COPY pom.xml pom.xml
RUN mvn -B -e dependency:go-offline

FROM maven:3.9-eclipse-temurin-21 AS app_builder
WORKDIR /opt/app
COPY --from=dependencies /root/.m2 /root/.m2
COPY pick-generator  pick-generator
COPY download-server  download-server
COPY pom.xml pom.xml
RUN mvn -B -e -T 1C  clean package -pl ./pick-generator/cli -am -DskipTests

FROM maven:3.9-eclipse-temurin-21 AS browser_installer_builder
WORKDIR /opt/app
COPY browser-installer ./
RUN mvn -B -e -T 1C  clean package -DskipTests

FROM eclipse-temurin:21 AS app
ARG app_version
ARG browser_installer_version="0.0.1"
WORKDIR /

COPY --from=browser_installer_builder /opt/app/target/browser-installer-fatjar-${browser_installer_version}.jar browser-installer-fatjar.jar
# playwright의 의존성들 다운로드
RUN java -jar browser-installer-fatjar.jar && rm browser-installer-fatjar.jar
# 아래의 환경변수를 세팅안해주면 firfox, webkit을 컨테이너를 실행할따마다 다운로드 받음
ENV PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=1

COPY --from=app_builder /opt/app/pick-generator/cli/target/cli-fatjar-${app_version}.jar cli.jar
ENV TZ="Asia/Seoul"
CMD java -jar cli.jar --include "hockey.*,basketball.*"
