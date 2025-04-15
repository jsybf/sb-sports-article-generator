package io.gitp.downloadserver

import io.ktor.http.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlin.io.path.deleteExisting


internal fun Route.downloadRoute(zipExporter: PicksZipExporter) = this.get("/download") {
    val zipPath = zipExporter.exportToTmpZip()

    call.response.header(
        HttpHeaders.ContentDisposition,
        ContentDisposition.Attachment.withParameter(ContentDisposition.Parameters.FileName, "picks.zip").toString()
    )
    call.respond(LocalPathContent(zipPath))

    zipPath.deleteExisting()
}