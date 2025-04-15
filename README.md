## what is it
스포츠 통계데이터들을 긁어모은뒤 llm에게 던져줘서 스포츠 배팅 추천글(픽이라고 부름) 작성을 자동화시켜주는 프로젝트

엔드유저는 연경제의 유망한 인재 단 한명

## how to use
의존성
- [just](https://just.systems/man/en/introduction.html)

우선 스크래핑 -> llm를 통한 문서생성 -> sqlite에 저장해주는 docker image 빌드
```bash
just pick-generator-docker-build
```
이제 실행해보자(이미지버전은 업데이트 될 수 있음 justfile참고)
```bash
docker run \
  -it \
  --rm \
  -v "$(pwd)":/opt/app/db \
  -e CLAUDE_API_KEY=<당신의 CLAUDE_API_KEY>\
  pick-generator:0.0.1
```