package me.nekomatamune.ygomaker

import mu.KotlinLogging
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

private val logger = KotlinLogging.logger { }

/**
 * A class to handle the backing up and rotation of files.
 */
class Backupper(
	private val backupDir: Path,
	private val numBackups: Int
) {

	/**
	 * Creates a backup files for [file] and rotates previous backups.
	 */
	fun backup(file: Path) {
		check(numBackups > 0)

		if (Files.notExists(backupDir)) {
			logger.info("Creating backup directory $backupDir")
			Files.createDirectory(backupDir)
		}

		// Rotate previously backed up files
		(numBackups - 2 downTo 0).map {
			backupFileFor(file, it) to backupFileFor(file, it + 1)
		}.filter {
			it.first.toFile().exists()
		}.forEach {
			logger.debug { "Rotating backup file ${it.first} to ${it.second}" }
			Files.move(it.first, it.second,
				StandardCopyOption.REPLACE_EXISTING,
				StandardCopyOption.ATOMIC_MOVE)
		}

		Files.copy(file, backupFileFor(file, 0))
	}

	private fun backupFileFor(file: Path, backupNumber: Int) =
		backupDir.resolve("${file.fileName}.${backupNumber}.bak")
}