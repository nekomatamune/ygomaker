package me.nekomatamune.ygomaker

import kotlinx.serialization.Serializable

/**
 * Encapsulates all the data of a card.
 */
@Serializable
data class Card(
	val name: String = "",
	val type: CardType = CardType.NORMAL_SUMMON_MONSTER,
	val monster: Monster? = Monster(),
	val image: Image? = null,
	val code: String = "",
	val effect: String = "",
	val serial: Int = 0
)

/**
 * Properties for monster cards.
 */
@Serializable
data class Monster(
	val attribute: Attribute = Attribute.LIGHT,
	val level: Int = 1,
	val type: String = "",
	val ability: String = "",
	val effect: Boolean = false,
	val atk: String = "",
	val def: String = "",
	val pendulum: Pendulum? = null,
	val links: Set<LinkMarker>? = null
)

/**
 * Properties for an image and its rendering details.
 */
@Serializable
data class Image(
	val file: String = "",
	val x: Int = 0,
	val y: Int = 0,
	val size: Int = 300
)

/**
 * Properties for [CardType.PENDULUM_MONSTER] card.
 */
@Serializable
data class Pendulum(
	val leftScale: Int = 0,
	val rightScale: Int = 0,
	val effect: String = ""
)

/**
 * Encapsulates a pack of cards.
 */
@Serializable
data class Pack(
	val name: String = "",
	val code: String = "",
	val language: Language = Language.JP,
	val copyright: String? = null,
	val cards: List<Card> = listOf()
)

/**
 * The card type, which determines the layout of the components.
 */
enum class CardType {
	NORMAL_SUMMON_MONSTER,
	SPECIAL_SUMMON_MONSTER, TOKEN_MONSTER, RITUAL_MONSTER, FUSION_MONSTER,
	SYNCHRO_MONSTER,
	XYZ_MONSTER, PENDULUM_MONSTER, LINK_MONSTER,
	NORMAL_SPELL, CONTINUOUS_SPELL, EQUIP_SPELL, QUICK_SPELL, RITUAL_SPELL,
	FIELD_SPELL,
	NORMAL_TRAP, CONTINUOUS_TRAP, COUNTER_TRAP,
}

/**
 * Attribute for monster cards.
 */
enum class Attribute {
	LIGHT, DARK, EARTH, WIND, WATER, FIRE, DIVINE,
}

/**
 * Allowed languages on the card.
 */
enum class Language {
	JP, EN
}

/**
 * Properties for [CardType.LINK_MONSTER] card.
 */
enum class LinkMarker {
	UP_LEFT, UP, UP_RIGHT, LEFT, RIGHT, DOWN_LEFT, DOWN, DOWN_RIGHT,
}

/**
 * Predefined value for [Monster.type]
 */
val MONSTER_TYPE_PRESETS = listOf(
	"ドラゴン族",
	"魔法使い族",
	"戦士族",
	"獣戦士族",
	"獣族",
	"鳥獣族",
	"アンデット族",
	"悪魔族",
	"天使族",
	"昆虫族",
	"恐竜族",
	"爬虫類族",
	"魚族",
	"海竜族",
	"機械族",
	"雷族",
	"水族",
	"炎族",
	"岩石族",
	"植物族",
	"サイキック族",
	"幻竜族",
	"サイバース族",
	"幻神獣族",
	"創造神族"
)

/**
 * Predefined value for [Monster.ability]
 */
val MONSTER_ABILITY_PRESETS = listOf(
	"", "リーバス", "トゥーン ", "スピリット", "ユニオン", "デュアル", "チューナー")

/**
 * [CardType] that are monsters. A [Card] with the following value should have
 * non-null [Card.monster].
 */
val MONSTER_CARD_TYPES = setOf(CardType.NORMAL_SUMMON_MONSTER,
	CardType.SPECIAL_SUMMON_MONSTER, CardType.TOKEN_MONSTER,
	CardType.RITUAL_MONSTER, CardType.FUSION_MONSTER, CardType.SYNCHRO_MONSTER,
	CardType.XYZ_MONSTER, CardType.PENDULUM_MONSTER, CardType.LINK_MONSTER)

val SPELL_CARD_TYPES = setOf(
	CardType.NORMAL_SPELL,
	CardType.CONTINUOUS_SPELL,
	CardType.EQUIP_SPELL,
	CardType.QUICK_SPELL,
	CardType.FIELD_SPELL,
	CardType.RITUAL_SPELL
)

val TRAP_CARD_TYPES = setOf(
	CardType.NORMAL_TRAP,
	CardType.CONTINUOUS_TRAP,
	CardType.COUNTER_TRAP
)



