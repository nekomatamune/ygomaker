package me.nekomatamune.ygomaker

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

private const val numBackups = 8

class Backupper(
	private val backupDir: Path,
	private val numBackups: Int
) {

	fun backup(file: Path) {
		check(numBackups > 0)

		// Rotate previously backed up files
		(numBackups-2 downTo 0).map {
			(backupFileFor(file, it) to backupFileFor(file, it + 1))
		}.filter {
			it.first.toFile().exists()
		}.forEach {
			Files.move(it.first, it.second,
				StandardCopyOption.REPLACE_EXISTING,
				StandardCopyOption.ATOMIC_MOVE)
		}

		Files.copy(file, backupFileFor(file, 0))
	}

	private fun backupFileFor(file: Path, backupNumber: Int) =
		backupDir.resolve("${file.fileName}.${backupNumber}.bak")
}