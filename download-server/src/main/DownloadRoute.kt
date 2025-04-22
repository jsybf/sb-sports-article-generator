package io.gitp.downloadserver

import io.gitp.sbpick.pickgenerator.database.repositories.PickRepository
import io.ktor.http.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlin.io.path.deleteExisting


internal fun Route.downloadRoute(pickRepo: PickRepository) = this.get("/download") {
    val zipPath = exportPicksToZip(pickRepo)

    call.response.header(
        HttpHeaders.ContentDisposition,
        ContentDisposition.Attachment.withParameter(ContentDisposition.Parameters.FileName, "picks.zip").toString()
    )
    call.respond(LocalPathContent(zipPath))

    zipPath.deleteExisting()
}