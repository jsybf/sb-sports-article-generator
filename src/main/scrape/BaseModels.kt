package scrape

import org.jsoup.nodes.Element

interface LLMQueryAttachment {
    fun toLLMQueryAttachment(): String
}

interface SportsWebPage {
    fun extractMeaningful(): Element
}



