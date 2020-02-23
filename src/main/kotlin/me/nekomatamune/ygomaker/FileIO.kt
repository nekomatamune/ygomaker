package me.nekomatamune.ygomaker

import javafx.scene.image.Image
import mu.KotlinLogging
import java.io.File
import java.io.FileNotFoundException

private val logger = KotlinLogging.logger { }

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
}