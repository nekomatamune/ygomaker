package me.nekomatamune.ygomaker

import org.spekframework.spek2.Spek
import strikt.api.expectThat
import strikt.assertions.containsExactly
import java.nio.file.Files
import java.nio.file.Path


object BackupperSpec : Spek({
	val backupContent = "The content to backup"


	group("a Backupper") {
		lateinit var fileToBackup: Path
		lateinit var sourceDir: Path
		lateinit var backupDir: Path
		lateinit var backupper: Backupper



		beforeEachTest {
			sourceDir = Files.createTempDirectory(null)
			sourceDir.toFile().deleteOnExit()
			backupDir = Files.createTempDirectory(null)
			backupDir.toFile().deleteOnExit()

			val sourceDir = Files.createTempDirectory(null)!!
			sourceDir.toFile().deleteOnExit()
			fileToBackup = sourceDir.resolve("pack.json")
			fileToBackup.toFile().writeText("my content")

			backupper = Backupper(backupDir, 3)
		}

		test("backup() should back up file when no previous backup exists") {

			backupper.backup(createFile(sourceDir, "pack.json", "backup 0"))

			expectThat(backupDir.toFile().listFiles()!!.map {
				it.toPath().fileName.toString() to it.readText()
			}).containsExactly("pack.json.0.bak" to "backup 0")
		}

		test("backup() should rotate file when previous backup exists") {
			createFile(backupDir, "pack.json.0.bak", "backup 1")

			backupper.backup(createFile(sourceDir, "pack.json", "backup 0"))

			expectThat(backupDir.toFile().listFiles()!!.map {
				it.toPath().fileName.toString() to it.readText()
			}).containsExactly(
				"pack.json.0.bak" to "backup 0",
				"pack.json.1.bak" to "backup 1"
			)
		}

		test("backup() should not exceed max backup number") {
			createFile(backupDir, "pack.json.0.bak", "backup 1")
			createFile(backupDir, "pack.json.1.bak", "backup 2")
			createFile(backupDir, "pack.json.2.bak", "backup 3")

			backupper.backup(createFile(sourceDir, "pack.json", "backup 0"))

			expectThat(backupDir.toFile().listFiles()!!.map {
				it.toPath().fileName.toString() to it.readText()
			}).containsExactly(
				"pack.json.0.bak" to "backup 0",
				"pack.json.1.bak" to "backup 1",
				"pack.json.2.bak" to "backup 2"
			)
		}


	}

})

private fun createFile(
	dir: Path,
	fileName: String,
	content: String
): Path {

	val file = dir.resolve(fileName)
	file.toFile().writeText(content)

	return file
}