package me.nekomatamune.ygomaker

import com.google.common.io.Resources
import javafx.scene.image.Image

fun getCardFrame(card: Card): Result<Image> {

	val frameUrl = when (card.type) {
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
		else -> "UNKNOWN"
	}.let {
		Resources.getResource("img/frame.${it}.png")
	}

	try {
		frameUrl.openStream().use {
			val frameImage = Image(it)
			return Result.success(frameImage)
		}
	} catch (e: Exception) {
		return Result.failure(e)
	}
}