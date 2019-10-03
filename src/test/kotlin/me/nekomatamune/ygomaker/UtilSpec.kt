package me.nekomatamune.ygomaker

import org.spekframework.spek2.Spek
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.isEqualTo

object UtilSpec : Spek({
	group("Card.fullCode()") {
		val pack = Pack(code = "TEST", language = Language.JP)
		val card = Card(code = "123")

		val cardFullCode = card.fullCode(pack)

		expectThat(cardFullCode).isEqualTo("TEST-JP123")
	}

	test("Card.toShortString() should work") {
		val card = Card(name = "my_name", code = "my_code")

		val shortString = card.toShortString()

		expectThat(shortString).isEqualTo("my_code my_name")
	}

})