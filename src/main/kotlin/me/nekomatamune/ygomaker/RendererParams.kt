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
	val levelSpacing: Int,

	val rankRect: Rect,
	val rankSpacing: Int,

	val spellTrapTypeRect: Rect,
	val spellTrapTypeFont: Font,
	val spellTrapTypeSymbolRect: Rect,

	val imageRect: Rect,

	val spellTrapEffectRect: Rect,
	val spellTrapEffectFont: Font,

	val monsterTypeRect: Rect,
	val monsterTypeFont: Font,
	val monsterEffectRect: Rect,
	val monsterEffectFont: Font,

	val atkRect: Rect,
	val defRect: Rect,
	val atkDefFont: Font

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