package io.gitp.downloadserver

import io.gitp.llmarticlewriter.database.SportsRepository
import io.gitp.llmarticlewriter.database.getSqliteConn
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.io.path.Path
import kotlin.io.path.createTempFile
import kotlin.io.path.writeText

private fun Path.toFis(): FileInputStream = FileInputStream(this.toFile())
private fun Path.toFos(): FileOutputStream = FileOutputStream(this.toFile())

/**
 * @param path: zip파일내에서의 경로
 */
private fun ZipOutputStream.putEntry(fis: FileInputStream, path: Path) {
    this.putNextEntry(ZipEntry(path.toString()))
    fis.copyTo(this)
    this.closeEntry()
}

/**
 * @param zipFile: 생성할 zip의 경로
 * @parm inputFIleList: Pair.first는 inputFile경로 Pair.second는 zip파일내에서 경로
 */
private fun Path.createZipFile(inputFileList: List<Pair<Path, Path>>): Path = ZipOutputStream(this.toFos()).use { zos ->
    inputFileList.forEach { (inputFile, pathInZip) ->
        zos.putEntry(fis = inputFile.toFis(), path = pathInZip)
    }
    this
}


/**
 * sqlite에 박혀있는 픽들을 zip파일로  export
 */
class PicksZipExporter(
    private val repo: SportsRepository
) {
    companion object {
        private val dateTimeFormat = DateTimeFormatter.ofPattern("yyyy.MM.dd-HH:mm:ss")
    }

    fun exportToTmpZip(zipPath: Path = createTempFile()): Path {
        val tmpDir: Path = Files.createTempDirectory("picks")
        val now = LocalDateTime.now()

        val articleFileList: List<Path> = repo
            .findMatchesHavingArticle()
            .filter { (matchInfo, _, article) -> now < matchInfo.startAt }
            .map { (matchInfo, _, article) ->
                val textFilePath: Path = tmpDir
                    .resolve(matchInfo.league.leagueName)
                    .resolve("${dateTimeFormat.format(matchInfo.startAt)}_${matchInfo.homeTeam}_${matchInfo.awayTeam}.txt")
                    .also { path -> Files.createDirectories(path.parent) }

                textFilePath.writeText(article.article)
                textFilePath
            }

        zipPath.createZipFile(
            articleFileList.map { file -> Pair(file, file.subpath(file.nameCount - 2, file.nameCount)) }
        )

        return zipPath
    }
}


fun main() {
    val service = getSqliteConn(Path("./test-data/sqlite.db"))
        .let { conn -> SportsRepository(conn) }
        .let { repo -> PicksZipExporter(repo) }

    service.exportToTmpZip()
}