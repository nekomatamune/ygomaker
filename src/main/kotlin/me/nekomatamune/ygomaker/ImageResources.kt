package me.nekomatamune.ygomaker

import com.google.common.io.Resources
import javafx.scene.image.Image

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