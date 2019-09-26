package me.nekomatamune.ygomaker

import org.spekframework.spek2.Spek
import strikt.api.expectThat
import strikt.assertions.hasEntry
import strikt.assertions.isEqualTo

object UtilSpec : Spek({
	group("Pack.copyWithFilledCardCodes()") {
		val basePack = Pack(
			code = "TEST",
			language = Language.JP
		)

		test("should fill code when not specified") {
			val pack = basePack.copy(
				cards = mapOf(
					0 to Card(),
					1 to Card(),
					999 to Card()
				)
			)

			val filledPack = pack.copyWithFilledCardCodes()

			expectThat(filledPack).get { cards.mapValues { it.value.code } }
				.hasEntry(0, "TEST-JP000")
				.hasEntry(1, "TEST-JP001")
				.hasEntry(999, "TEST-JP999")
		}

		test("should not fill code when already specified") {
			val pack = basePack.copy(
				cards = mapOf(0 to Card(code = "my_code"))
			)

			val filledPack = pack.copyWithFilledCardCodes()

			expectThat(filledPack).get { cards.mapValues { it.value.code } }
				.hasEntry(0, "my_code")
		}
	}

	test("Card.toShortString() should work") {
		val card = Card(name="my_name", code="my_code")

		val shortString = card.toShortString()

		expectThat(shortString).isEqualTo("my_code my_name")
	}

})