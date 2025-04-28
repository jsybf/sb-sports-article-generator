package io.gitp.sbpick.pickgenerator.scraper.scrapebase.browser

import com.microsoft.playwright.Page
import com.microsoft.playwright.TimeoutError
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.logger
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.sync.Semaphore
import org.jsoup.nodes.Document

class PlaywrightBrowserPool(
    private val workerNum: Int
) : AutoCloseable {
    @OptIn(DelicateCoroutinesApi::class)
    private val playwrightDispatcher = newFixedThreadPoolContext(workerNum, "browser-pool")
    private val semaphore = Semaphore(workerNum)
    private val workerChannel = Channel<PlaywrightBrowser>(workerNum)
    private val scope = CoroutineScope(SupervisorJob() + playwrightDispatcher)

    init {
        runBlocking {
            repeat(workerNum) {
                scope.launch { workerChannel.send(PlaywrightBrowser()) }
            }
        }
    }

    private suspend fun scrapeRetry(retry: Int, block: suspend () -> Document): Document {
        for (tryCnt in 1..retry) {
            val result: Result<Document> = runCatching { block() }
            result.onSuccess { return it }
            result.onFailure { exception: Throwable ->
                if (exception !is TimeoutError) throw exception
                logger.warn("got ${exception} try_cnt:${tryCnt}")
            }
        }
        throw Exception("exceed max retry")
    }

    suspend fun doAndGetDoc(action: suspend Page.() -> Unit): Document {
        semaphore.acquire()
        val worker = workerChannel.receive()
        try {
            return scrapeRetry(2) { worker.doAndGetDoc(action) }
        } finally {
            workerChannel.send(worker)
            semaphore.release()
        }
    }

    fun doAndGetDocAsync(action: suspend Page.() -> Unit): Deferred<Document> = scope.async { doAndGetDoc(action) }

    override fun close() {
        scope.cancel()
        runBlocking {
            while (!workerChannel.isEmpty) {
                val worker = workerChannel.receive()
                worker.close()
            }
        }
        workerChannel.close()
    }
}