## 책임

[flashsocre](https://www.flashscore.co.kr/) 웹사이트에서 문서 생성에 필요한 페이지들을 db에 저장하는 것

## 기능

- 스크래핑
- html -> json 추출

## 메모

html 을 그대로 llm에 넘겨주면 토큰이 너무 많기 때문에 일차적으로 html -> json 변환을 함.
html -> json 코드는 llm을 통해 작성