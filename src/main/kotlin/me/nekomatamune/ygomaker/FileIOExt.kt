package me.nekomatamune.ygomaker

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import java.io.FileNotFoundException
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes

private val backupper by lazy {
	Backupper(Command.dataDir.resolve("bak"), 10)
}
private val json = Json(JsonConfiguration.Stable.copy(prettyPrint = true))

fun Path.deepCopyTo(toPath: Path) {
	Files.walkFileTree(this, object : SimpleFileVisitor<Path>() {
		override fun visitFile(
			file: Path, attrs: BasicFileAttributes
		): FileVisitResult {
			Files.copy(file,
				toPath.resolve(file.fileName),
				StandardCopyOption.REPLACE_EXISTING
			)
			return FileVisitResult.CONTINUE
		}
	})
}

fun Pack.writeTo(packDir: Path) {
	val cardFile = packDir.resolve("pack.json")

	backupper.backup(cardFile)

	val packJson = json.stringify(Pack.serializer(), this)

	cardFile.toFile().writeText(packJson)
}

fun Pack.Companion.readFrom(packDir: Path): Pack {
	val cardFile = packDir.resolve("pack.json")
	if (!cardFile.toFile().isFile) {
		throw FileNotFoundException(cardFile.toString())
	}
	return json.parse(Pack.serializer(), cardFile.toFile().readText())
}
