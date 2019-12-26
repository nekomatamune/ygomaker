package me.nekomatamune.ygomaker

import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes

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
