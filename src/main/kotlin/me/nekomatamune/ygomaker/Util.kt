package me.nekomatamune.ygomaker

fun Card.toShortString() = "$code $name"

fun Pack.copyWithFilledCardCodes(): Pack {
	return this.copy(
		cards = this.cards.mapValues { cardEntry ->
			cardEntry.value.copy(
				code = cardEntry.value.code.ifEmpty {
					"${this.code}-${this.language}${"%03d".format(cardEntry.key)}"
				}
			)
		}
	)
}

fun CardType.isMonster() = (this in MONSTER_CARD_TYPES)