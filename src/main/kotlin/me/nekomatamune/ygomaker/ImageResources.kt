package me.nekomatamune.ygomaker

import com.google.common.io.Resources
import javafx.scene.image.Image
import javafx.scene.paint.Color

fun getCardFrame(card: Card): Result<Image> {

	val frameTag = when (card.type) {
		CardType.NORMAL_SUMMON_MONSTER ->
			if (card.monster!!.effect) "effect"
			else "normal"
		CardType.SPECIAL_SUMMON_MONSTER -> "effect"
		CardType.RITUAL_MONSTER -> "ritual"
		CardType.FUSION_MONSTER -> "fusion"
		CardType.SYNCHRO_MONSTER -> "synchro"
		CardType.XYZ_MONSTER -> "xyz"
		in SPELL_CARD_TYPES -> "spell"
		in TRAP_CARD_TYPES -> "trap"
		else -> return Result.failure(
			IllegalArgumentException("MonsterType ${card.type} is not supported yet.")
		)
	}

	try {
		Resources.getResource("img/frame.${frameTag}.png").openStream().use {
			val frameImage = Image(it)
			return Result.success(frameImage)
		}
	} catch (e: Exception) {
		return Result.failure(e)
	}
}

fun getCardNameColor(card: Card): Color {
	return when(card.type) {
		in SPELL_CARD_TYPES -> Color.WHITE
		in TRAP_CARD_TYPES -> Color.WHITE
		CardType.XYZ_MONSTER -> Color.WHITE
		else -> Color.BLACK
	}
}

fun getAttribute(card: Card): Result<Image> {
	val tag = when(card.type) {
		in SPELL_CARD_TYPES -> "spell"
		in TRAP_CARD_TYPES -> "trap"
		else -> when(card.monster!!.attribute) {
			Attribute.LIGHT -> "light"
			Attribute.DARK -> "dark"
			Attribute.WATER -> "water"
			Attribute.FIRE -> "fire"
			Attribute.EARTH -> "earth"
			Attribute.WIND -> "wind"
			Attribute.DIVINE -> "divine"
		}
	}

	try {
		Resources.getResource("img/attribute.${tag}.png").openStream().use {
			val attributeImage = Image(it)
			return Result.success(attributeImage)
		}
	} catch(e: Exception) {
		return Result.failure(e)
	}
}

fun getSymbol(card: Card): Result<Image?> {
	val tag = when(card.type) {
		CardType.CONTINUOUS_SPELL -> "continuous"
		CardType.CONTINUOUS_TRAP -> "continuous"
		CardType.EQUIP_SPELL -> "equip"
		CardType.QUICK_SPELL -> "quickplay"
		CardType.FIELD_SPELL -> "field"
		CardType.RITUAL_SPELL -> "ritual"
		CardType.XYZ_MONSTER -> "rank"
		in MONSTER_CARD_TYPES -> "level"
		else -> return Result.success(null)
	}

	try {
		Resources.getResource("img/symbol.${tag}.png").openStream().use {
			val symbolImage = Image(it)
			return Result.success(symbolImage)
		}
	} catch(e: Exception) {
		return Result.failure(e)
	}

}