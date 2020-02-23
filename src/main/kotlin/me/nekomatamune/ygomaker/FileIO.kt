package me.nekomatamune.ygomaker

import javafx.scene.image.Image
import kotlinx.io.IOException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import mu.KotlinLogging
import java.io.File
import java.io.FileNotFoundException
import java.nio.file.Path

private val logger = KotlinLogging.logger { }
private val json = Json(JsonConfiguration.Stable.copy(prettyPrint = true))
private const val PACK_JSON_BASENAME = "pack.json"

open class FileIO {

	fun readImage(file: File): Result<Image> {
		logger.debug { "Reading image from file: $file" }

		if (!file.exists()) {
			return failure(FileNotFoundException(file.toString()))
		}
		if (!file.isFile) {
			return failure(IllegalArgumentException("Not a file: $file"))
		}

		return try {
			val image = Image(file.toURI().toString())
			success(image)
		} catch (error: Exception) {
			failure(error)
		}
	}

	fun readPack(packDir: Path): Result<Pack> {
		logger.info { "Read pack from $packDir" }

		if(!packDir.toFile().isDirectory) {
			return failure(IllegalArgumentException("Not a directory: $packDir"))
		}

		val packJsonFile = packDir.resolve(PACK_JSON_BASENAME)

		val pack = try {
			val packText = packJsonFile.toFile().readText()
			json.parse(Pack.serializer(), packText)

		} catch (e: Exception) {
			return failure(e)
		}

		return success(pack)
	}

	fun writePack(pack: Pack, packDir: Path): Result<Unit> {
		logger.info { "Write pack to $packDir" }

		val packJson = json.stringify(Pack.serializer(), pack)
		val packJsonFile = packDir.resolve(PACK_JSON_BASENAME).toFile()

		return try {
			packJsonFile.writeText(packJson)
			success()
		} catch (e: IOException) {
			failure(e)
		}

	}
}