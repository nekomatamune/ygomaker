package me.nekomatamune.ygomaker

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

private val backupDir by lazy { Command.dataDir.resolve("bak") }
private const val numBackups = 8

class Backupper {

	fun backup(file: Path) {
		// Rotate previously backed up files
		(numBackups downTo 0).map {
			(backupFileFor(file, it) to backupFileFor(file, it + 1))
		}.filter {
			it.first.toFile().exists()
		}.forEach {
			Files.move(it.first, it.second,
				StandardCopyOption.REPLACE_EXISTING,
				StandardCopyOption.ATOMIC_MOVE)
		}

		Files.copy(file, backupFileFor(file, 0),
			StandardCopyOption.COPY_ATTRIBUTES,
			StandardCopyOption.ATOMIC_MOVE
		)
	}

	private fun backupFileFor(file: Path, backupNumber: Int) =
		backupDir.resolve("${file.fileName}.${backupNumber}.bak")
}