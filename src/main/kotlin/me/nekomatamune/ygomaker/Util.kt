package me.nekomatamune.ygomaker

fun Card.toShortString() = "$code $name"

fun Card.fullCode(pack: Pack) = "${pack.code}-${pack.language}${this.code}"

fun CardType.isMonster() = (this in MONSTER_CARD_TYPES)

fun <T> Result<T>.continueOnSuccess(action: () -> Result<T>): Result<T> {
	return if (this.isFailure) this else action()
}