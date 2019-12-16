package me.nekomatamune.ygomaker

import mu.KotlinLogging

private val logger = KotlinLogging.logger {  }

typealias WidthMeasure = (String) -> Float
typealias HeightMeasure = () -> Float

data class MultilineText(
	val size: Int,
	val lines: List<String>
)

fun toMultilineText(
	text: String, sizes: List<Int>,
	width: Int, height: Int,
	getWidthMeasure: (Int) -> WidthMeasure,
	getHeightMeasure: (Int) -> HeightMeasure
): MultilineText {

	var lines = listOf<String>()
	for (size in sizes) {
		logger.info { "Trying size $size" }
		lines = split(text, width, getWidthMeasure(size))
		if (lines.size * getHeightMeasure(size)() < height) {
			return MultilineText(size, lines)
		}
	}

	return MultilineText(sizes.last(), lines)

}

private fun split(
	text: String, width: Int, widthMeasure: WidthMeasure): List<String> {

	val lines = mutableListOf<String>()
	var currentLine = ""
	for (ch in text) {
		if (ch == '\n') {
			lines.add(currentLine)
			currentLine = ""
			continue
		}

		val nextCurrentLine = currentLine + ch
		logger.info { "nextCurrentLine: $nextCurrentLine" }
		if (widthMeasure(nextCurrentLine) > width) {
			lines.add(currentLine)
			currentLine = ch.toString()
			continue
		}

		currentLine = nextCurrentLine
	}

	if (currentLine.isNotBlank()) {
		lines.add(currentLine)
	}

	return lines

}