# what is it
스포츠 통계데이터들을 긁어모은뒤 llm에게 던져줘서 스포츠 배팅 추천글(픽이라고 부름) 작성을 자동화시켜주는 프로젝트

엔드유저는 연경제의 유망한 인재 단 한명

---

# how to 
## run pick-generator in local
`local-pick-generator-run`의 첫번째 매개변수는 [[#pick-generator cli]]참고
```bash
just comppile-pick-generator
just local-mysql-up
just local-pick-generator-run "INPUT_YOUR_PARAM" "INPUT_YOUR_CLAUDE_API_KEY"
just local-mysql-down
```

## how to push pick-generator image
```bash
just push-pick-generator
```

---

# reference

## pick-generator cli

`pick-generator`는 fat-jar cli로 스포츠 통계사이트들을 스크래핑 한뒤 llm에게 이 정보를 넘겨주고 pick(배팅 추천글 생성)결과값을 받아 db에 저장해주는 cli.

실행을 하기 위해서는 db와 claude api key가 환경변수로 지정되어야 한다.

```bash
export SB_PICK_MYSQL_HOST=""
export SB_PICK_MYSQL_PORT=""
export SB_PICK_MYSQL_USER=""
export SB_PICK_MYSQL_PW=""
export SB_PICK_MYSQL_DB=""
export SB_PICK_CLAUDE_API_KEY=""
```
환경변수가 지정되었다면 로컬에서 아래와 같은 형태로 실행이 가능하다.
```bash
java -jar ./pick-generator/cli/target/cli-fatjar-0.0.1.jar [<--all>] [<--include COMMA_SEPERATED_LEAGUES>] [<--exclude COMMA_SEPERATED_LEAGUES>]
```
작업의 대상이 될 리그들은 `<SPORTS_NAME>.<LEAGUE_NAME>` 형태의 표현식으로 지정/제외한다.

실행예시들
```bash
java -jar cli-fatjar-0.0.1.jar --all # 모든 리그
java -jar cli-fatjar-0.0.1.jar --include "basketball.*" # 야구의 모든 리그
java -jar cli-fatjar-0.0.1.jar --include "basketball.NPB,hockey.*" # NPB(일본 야구리그), 하키의 모든리그
java -jar cli-fatjar-0.0.1.jar --include "baseball.*,hockey.*" --exclude "hockey.KHL" # 야구, 하키의 모든리그 중 KHL리그 제외
```
