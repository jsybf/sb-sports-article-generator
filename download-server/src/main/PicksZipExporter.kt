package io.gitp.downloadserver

import io.gitp.sbpick.pickgenerator.database.repositories.PickRepository
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.time.format.DateTimeFormatter
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.io.path.createDirectories
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


private val dateTimeFormat = DateTimeFormatter.ofPattern("yyyy.MM.dd-HH.mm.ss")

/**
 * db에서 앞으로 있을 경기들의 pick을 zip파일로 저장.
 * ./<league_name>/<datetime>_<home_team><away_team>.txt 형식으로 zip파일에 저장됨
 * (example) ./KBO/2025.04.24-18.30.00_삼성_____KIA___.txt
 * 호출함수가 zip파일을 사용한뒤 지워야한다.
 */
fun exportPicksToZip(pickRepo: PickRepository): Path {
    val tmpDir: Path = Files.createTempDirectory(null)

    val picks = pickRepo.findFixturesHavingPick()
    val homeTeamMaxNameLen = picks.map { (match, _) -> match.homeTeam.length }.maxOrNull()!!
    val awayTeamMaxNameLen = picks.map { (match, _) -> match.awayTeam.length }.maxOrNull()!!
    val pickFiles = picks
        .map { (match, pick) ->
            val pickFileName = "${dateTimeFormat.format(match.matchAt)}_${match.homeTeam.padEnd(homeTeamMaxNameLen, '_')}_${match.awayTeam.padEnd(awayTeamMaxNameLen, '_')}.txt"
            val pickFile = tmpDir.resolve(match.league.leagueName).resolve(pickFileName)

            pickFile.parent.createDirectories()
            pickFile.writeText(pick.content)

            pickFile
        }

    val zipPath: Path = Files.createTempFile(null, null).toAbsolutePath()
    zipPath.createZipFile(
        pickFiles.map { file -> Pair(file, file.subpath(file.nameCount - 2, file.nameCount)) }
    )

    tmpDir.toFile().deleteRecursively()
    return zipPath
}
