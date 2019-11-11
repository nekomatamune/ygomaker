package me.nekomatamune.ygomaker

import kotlinx.serialization.Serializable

@Serializable
data class RendererParams(
	val frameOrigin: Point,
	val frameSize: Size,

	val nameOrigin: Point,
	val nameFont: Font
)

@Serializable
data class Size(
	val w: Double,
	val h: Double
)

@Serializable
data class Point(
	val x: Double,
	val y: Double
)

@Serializable
data class Font(
	val name: String,
	val size: Double,
	val bold: Boolean = false,
	val italic: Boolean = false
)