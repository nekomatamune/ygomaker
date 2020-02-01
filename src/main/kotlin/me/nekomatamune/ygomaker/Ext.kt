package me.nekomatamune.ygomaker

import mu.KotlinLogging
import java.nio.file.Path

private val logger = KotlinLogging.logger { }

fun Card.toShortString() = "$code $name"

fun Card.fullCode(pack: Pack) = "${pack.code}-${pack.language}${this.code}"

fun CardType.isMonster() = (this in MONSTER_CARD_TYPES)

fun Result.Companion.ok() = success(Unit)

fun <T> Result<T>.logFailure() = this.onFailure { logger.error(it) {} }

fun <T> Result<T>.then(action: () -> Result<T>): Result<T> {
	return if (this.isFailure) this else action()
}

fun Path.toAbsNormPath() = this.toAbsolutePath().normalize()