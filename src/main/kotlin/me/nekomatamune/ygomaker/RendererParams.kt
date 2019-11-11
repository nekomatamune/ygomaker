package me.nekomatamune.ygomaker

import kotlinx.serialization.Serializable

@Serializable
data class RendererParams(
	val frameOrigin: Point,
	val frameSize: Size
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