package me.nekomatamune.ygomaker

import org.spekframework.spek2.Spek
import strikt.api.expectThat
import strikt.assertions.hasEntry
import strikt.assertions.hasSize
import strikt.assertions.isEmpty
import java.nio.file.Files
import java.nio.file.Path
import kotlin.streams.toList


object BackupperSpec : Spek({

	test("backup() should copy file and rotate existing backups") {
		val sourceDir = Files.createTempDirectory(null)
		val backupDir = Files.createTempDirectory(null)
		sequenceOf(sourceDir, backupDir).forEach { it.toFile().deleteOnExit() }

		val backupper = Backupper(backupDir, 3)
		expectThat(Files.list(backupDir).toList()).isEmpty()

		backupper.backup(createFile(sourceDir, "myfile", "v0"))
		expectThat(Files.list(backupDir)
			.map { it.fileName.toString() to it.toFile().readText() }.toList().toMap())
			.hasEntry("myfile.0.bak", "v0")
			.hasSize(1)

		backupper.backup(createFile(sourceDir, "myfile", "v1"))
		expectThat(Files.list(backupDir)
			.map { it.fileName.toString() to it.toFile().readText() }.toList().toMap())
			.hasEntry("myfile.0.bak", "v1")
			.hasEntry("myfile.1.bak", "v0")
			.hasSize(2)

		backupper.backup(createFile(sourceDir, "myfile", "v2"))
		expectThat(Files.list(backupDir)
			.map { it.fileName.toString() to it.toFile().readText() }.toList().toMap())
			.hasEntry("myfile.0.bak", "v2")
			.hasEntry("myfile.1.bak", "v1")
			.hasEntry("myfile.2.bak", "v0")
			.hasSize(3)

		backupper.backup(createFile(sourceDir, "myfile", "v3"))
		expectThat(Files.list(backupDir)
			.map { it.fileName.toString() to it.toFile().readText() }.toList().toMap())
			.hasEntry("myfile.0.bak", "v3")
			.hasEntry("myfile.1.bak", "v2")
			.hasEntry("myfile.2.bak", "v1")
			.hasSize(3)
	}

	test("backup() should not mix backups of different files") {
		val sourceDir = Files.createTempDirectory(null)
		val backupDir = Files.createTempDirectory(null)
		sequenceOf(sourceDir, backupDir).forEach { it.toFile().deleteOnExit() }

		val backupper = Backupper(backupDir, 3)
		expectThat(Files.list(backupDir).toList()).isEmpty()

		backupper.backup(createFile(sourceDir, "myfile", "v0"))
		backupper.backup(createFile(sourceDir, "yourfile", "v0"))
		backupper.backup(createFile(sourceDir, "myfile", "v1"))
		backupper.backup(createFile(sourceDir, "yourfile", "v1"))

		expectThat(Files.list(backupDir)
			.map { it.fileName.toString() to it.toFile().readText() }.toList().toMap())
			.hasEntry("myfile.0.bak", "v1")
			.hasEntry("myfile.1.bak", "v0")
			.hasEntry("yourfile.0.bak", "v1")
			.hasEntry("yourfile.1.bak", "v0")
			.hasSize(4)
	}
})

private fun createFile(dir: Path, fileName: String, content: String): Path {
	val file = dir.resolve(fileName)
	file.toFile().writeText(content)
	return file
}