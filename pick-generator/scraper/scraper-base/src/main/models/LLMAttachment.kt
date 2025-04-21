package io.gitp.sbpick.pickgenerator.scraper.scrapebase.models

/**
 * llm에게 던질 쿼리로 변환하는 인터페이스.
 * claude는 xml로 미리 쿼리받을 데이터항목들을 지정하고 이 항목들만 던져주면 알아서 해석해준다.
 * [LLMAttachment.toLLMAttachment]의 출력예시는 claude를 사용한다면 아래와 같을 수 있겠다.
 * """
 *  <matchSummary>
 *      ~~~
 *  </matchSummary>
 *  <oneXTwoBet>
 *      ~~~
 *  </oneXTwoBet>
 * """
 *
 */
interface LLMAttachment {
    fun toLLMAttachment(): String
}