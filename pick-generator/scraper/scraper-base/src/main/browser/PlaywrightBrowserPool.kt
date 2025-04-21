package io.gitp.sbpick.pickgenerator.scraper.scrapebase.browser

import com.microsoft.playwright.Page
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.logger
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.sync.Semaphore
import org.jsoup.nodes.Document

class PlaywrightBrowserPool(
    private val workerNum: Int
) : AutoCloseable {
    private val playwrightDispatcher = Dispatchers.IO.limitedParallelism(workerNum, "playwright-browser-pool")
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

    suspend fun doAndGetDoc(action: suspend Page.() -> Unit): Document {
        semaphore.acquire()
        val worker = workerChannel.receive()
        try {
            logger.trace("executing doAndGetDoc")
            return worker.doAndGetDoc(action)
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