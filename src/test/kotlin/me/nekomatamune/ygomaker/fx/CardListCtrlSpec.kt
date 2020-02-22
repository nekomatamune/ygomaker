package me.nekomatamune.ygomaker.fx

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import javafx.scene.control.ListView
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCode.*
import me.nekomatamune.ygomaker.Card
import me.nekomatamune.ygomaker.Language
import me.nekomatamune.ygomaker.Pack
import me.nekomatamune.ygomaker.Result
import me.nekomatamune.ygomaker.success
import org.spekframework.spek2.Spek
import org.testfx.api.FxRobot
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.isEqualTo
import strikt.assertions.map

object CardListCtrlSpec : Spek({
	setupTestFx<CardListCtrl>("fx/CardList.fxml", mapOf(
			CardListCtrl::class to { CardListCtrl() }
	))

	val ctrl by memoized<CardListCtrl>()
	val robot by memoized<FxRobot>()

	val kSomeCard = Card(name = "someCardName")
	val kSomeCards = (1..10).map { kSomeCard.copy(name = "someCard$it") }
	val kSomePack = Pack(
			name = "somePackName",
			code = "PACK",
			language = Language.JP,
			cards = kSomeCards
	)

	val mockCardSelectedHandler = mockk<(Card) -> Result<Unit>>()
	val cardLists = mutableListOf<Card>()

	beforeEachTest {
		every { mockCardSelectedHandler(any()) }.returns(success())
		ctrl.cardSelectedHandler = mockCardSelectedHandler

		robot.interact { ctrl.setState(kSomePack) }
	}

	group("#setState") {
		test("Should populate card list") {
			val myPack = kSomePack.copy(
					cards = (1..20).map {
						kSomeCard.copy(name = "My Card $it")
					}
			)

			robot.interact { ctrl.setState(myPack) }

			robot.lookupAs<ListView<Card>>("#cardListView").let {
				expectThat(it.items).containsExactly(myPack.cards)
			}
		}
	}

	test("Should invoke cardSelectedHandler") {
		val myCardName = (1..5).map { "My Card #$it" }

		robot.interact {
			ctrl.setState(kSomePack.copy(
					cards = myCardName.map { kSomeCard.copy(name = it) }
			))
			robot.lookupAs<ListView<Card>>("#cardListView").requestFocus()
		}

		robot.type(DOWN, UP).type(DOWN, 4)

		verify(exactly = 6) { mockCardSelectedHandler(capture(cardLists)) }
		expectThat(cardLists).map { it.name }.containsExactly(
				myCardName[1],
				myCardName[0],
				myCardName[1],
				myCardName[2],
				myCardName[3],
				myCardName[4]
		)
	}

})