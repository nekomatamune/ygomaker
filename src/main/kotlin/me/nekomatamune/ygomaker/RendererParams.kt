package me.nekomatamune.ygomaker

import kotlinx.serialization.Serializable

@Serializable
data class RendererParams(
	val frameOrigin: Point,
	val frameSize: Size,

	val nameRect: Rect,
	val nameFont: Font,

	val attributeRect: Rect,

  val levelRect: Rect,
	val levelSpacing: Int
)

@Serializable
data class Rect(
	val x: Double,
	val y: Double,
	val w: Double,
	val h: Double
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
	val italic: Boolean = false,
	val tracking: Int = 0
)