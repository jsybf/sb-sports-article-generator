package io.gitp.llmarticlewrtier.spojoyscraper

import com.microsoft.playwright.Page
import com.microsoft.playwright.Playwright
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.slf4j.LoggerFactory
import java.util.concurrent.*

class PlaywrightWorker : AutoCloseable {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private val playwright: Playwright = Playwright.create()
    private val browser = playwright.chromium().launch()

    fun doAndGetDoc(actions: Page.() -> Unit): Document {
        val page = browser.newPage()
        page.actions()
        Playwright.create().use { }

        val html = page.content().let { Jsoup.parse(it) }
        page.close()
        return html
    }

    override fun close() {
        browser.close()
        playwright.close()
        logger.debug("closed PlayWrightWorker(${this})")
    }
}

class PlaywrightWorkerPool(
    private val workerNum: Int
) : AutoCloseable {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private val executorService: ExecutorService
    private val workerQueue: BlockingQueue<PlaywrightWorker>
    private val workerPool: List<PlaywrightWorker>

    init {
        this.executorService = Executors.newFixedThreadPool(workerNum)
        this.workerPool = List(workerNum) { PlaywrightWorker() }
        this.workerQueue = ArrayBlockingQueue(workerNum, true, this.workerPool)
    }

    fun doAndGetDoc(actions: Page.() -> Unit): Document {
        val worker = workerQueue.take() // blocking code
        logger.debug("using ${workerNum - workerQueue.size}/${workerNum}")
        return try {
            worker.doAndGetDoc(actions)
        } finally {
            workerQueue.put(worker)
        }
    }

    fun submitTask(actions: Page.() -> Unit): Future<Document> {
        return executorService.submit<Document> { this.doAndGetDoc(actions) }
    }

    override fun close() {
        executorService.shutdown()
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow()
            }
        } catch (e: InterruptedException) {
            executorService.shutdownNow()
            Thread.currentThread().interrupt()
        }

        workerPool.forEach { it.close() }
    }
}

private val logger = LoggerFactory.getLogger(object {}::class.java.packageName)

private fun main() {
    PlaywrightWorkerPool(3).use { browserWorkerPool ->
        val doc1: Future<Document> = browserWorkerPool.submitTask {
            logger.info("requesting1 spojoy baseball page(https://www.spojoy.com/live/?mct=baseball#rs)")
            navigate("https://www.spojoy.com/live/?mct=baseball#rs")
        }
        val doc2: Future<Document> = browserWorkerPool.submitTask {
            logger.info("requesting2 spojoy baseball page(https://spodb.spojoy.com/?game_id=402775#google_vignette)")
            navigate("https://spodb.spojoy.com/?game_id=402775#google_vignette")
        }

        doc1.get()
        doc2.get().let { println(it) }
        // println("${doc1.get()}")
    }
}