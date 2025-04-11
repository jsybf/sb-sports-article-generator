package db

import org.jetbrains.exposed.sql.ColumnTransformer
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class HtmlColumnTransformer : ColumnTransformer<String, Document> {
    override fun unwrap(value: Document): String = value.html()
    override fun wrap(value: String): Document = Jsoup.parse(value)
}