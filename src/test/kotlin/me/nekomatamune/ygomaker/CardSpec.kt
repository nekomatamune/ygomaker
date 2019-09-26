package me.nekomatamune.ygomaker

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import org.spekframework.spek2.Spek
import strikt.api.expectThat
import strikt.assertions.*

object CardSpec : Spek({
	test("a card can be parsed from json") {
		val jsonText = """ 
				{
					"name": "my_card_name",
					"type": "SPECIAL_SUMMON_MONSTER",
					"monster": {
						"attribute": "DARK",
						"level": 10,
						"type": "my_type",
						"ability": "my_ability",
						"effect": true,
						"atk": "3000",
						"def": "2500",
						"pendulum": {
							"leftScale": 0,
							"rightScale": 13,
							"effect": "my_pendulum_effect"
						},
						"links": ["UP", "DOWN_RIGHT"]
					},
					"image": {
						"file": "my_image.jpg",
						"x": 123,
						"y": 456,
						"size": 789
					},
					"code": "TEST-JP123",
					"effect": "my_effect",
					"serial": 12344321
				}
			""".trimIndent()

		val parsedCard = Json(JsonConfiguration.Stable)
			.parse(Card.serializer(), jsonText)

		expectThat(parsedCard) {
			get { name }.isEqualTo("my_card_name")
			get { type }.isEqualTo(CardType.SPECIAL_SUMMON_MONSTER)
			get { monster }.isNotNull().and {
				get { attribute }.isEqualTo(Attribute.DARK)
				get { level }.isEqualTo(10)
				get { type }.isEqualTo("my_type")
				get { ability }.isEqualTo("my_ability")
				get { effect }.isTrue()
				get { atk }.isEqualTo("3000")
				get { def }.isEqualTo("2500")
				get { pendulum }.isNotNull().and {
					get { leftScale }.isEqualTo(0)
					get { rightScale }.isEqualTo(13)
					get { effect }.isEqualTo("my_pendulum_effect")
				}
				get { links }.isNotNull().contains(LinkMarker.UP, LinkMarker.DOWN_RIGHT)
			}
			get { image }.isNotNull().and {
				get { file }.isEqualTo("my_image.jpg")
				get { x }.isEqualTo(123)
				get { y }.isEqualTo(456)
				get { size }.isEqualTo(789)
			}
			get { code }.isEqualTo("TEST-JP123")
			get { effect }.isEqualTo("my_effect")
			get { serial }.isEqualTo(12344321)
		}
	}

	test("a pack can be parsed from json") {
		val jsonText = """
			{
				"name": "my_pack_name",
				"code": "my_pack_code",
				"language": "EN",
				"copyright": "my_copyright",
				"cards": {
				  001: {
						"name": "card_01"
					},
					002: {
						"name": "card_02"
					}
				}
			}
		""".trimIndent()

		val parsedPack = Json(JsonConfiguration.Stable)
			.parse(Pack.serializer(), jsonText)

		expectThat(parsedPack) {
			get { name }.isEqualTo("my_pack_name")
			get { code }.isEqualTo("my_pack_code")
			get { language }.isEqualTo(Language.EN)
			get { copyright }.isEqualTo("my_copyright")
			get { cards.mapValues { it.value.name } }
				.hasEntry(1, "card_01")
				.hasEntry(2, "card_02")
				.hasSize(2)
		}
	}
})