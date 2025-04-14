package io.gitp.llmarticlewriter.database

import org.jetbrains.exposed.sql.IColumnType
import org.jetbrains.exposed.sql.SqlLogger
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.statements.StatementContext
import java.util.*


/**
 * 길이가 너무긴(100이상) sql 인자들은 ???로 치환
 * [org.jetbrains.exposed.sql.statements.StatementContext.expandArgs]를 참고해서 구현
 */
private fun StatementContext.expandShortArgs(transaction: Transaction): String {
    val sql = sql(transaction)
    val iterator = args.iterator()

    if (!iterator.hasNext()) return sql

    return buildString {
        val quoteStack = Stack<Char>()
        var lastPos = 0

        var i = -1
        while (++i < sql.length) {
            val char = sql[i]
            when {
                char == '?' && quoteStack.isEmpty() -> {
                    if (sql.getOrNull(i + 1) == '?') {
                        i++
                        continue
                    }
                    append(sql.substring(lastPos, i))
                    lastPos = i + 1
                    val (col, value) = iterator.next()
                    // 수정된 부분
                    (col as IColumnType<Any>).valueToString(value).let { arg ->
                        if (arg.length < 100) append(arg)
                        else append("???")
                    }
                }
                char == '\'' || char == '\"' -> {
                    when {
                        quoteStack.isEmpty() -> quoteStack.push(char)
                        quoteStack.peek() == char -> quoteStack.pop()
                        else -> quoteStack.push(char)
                    }
                }
            }
        }

        if (lastPos < sql.length) {
            append(sql.substring(lastPos))
        }
    }
}

object ExposedLogger : SqlLogger {
    override fun log(context: StatementContext, transaction: Transaction) {
        println("SQL: ${context.expandShortArgs(transaction)}")
    }
}