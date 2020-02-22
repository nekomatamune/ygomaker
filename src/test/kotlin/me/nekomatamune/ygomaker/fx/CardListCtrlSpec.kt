package me.nekomatamune.ygomaker.fx

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import javafx.scene.control.ListView
import javafx.scene.input.KeyCode.DOWN
import javafx.scene.input.KeyCode.UP
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

		robot.interact { ctrl.updatePack(kSomePack) }
	}

	group("#setPack") {
		test("Should populate card list") {
			val myPack = kSomePack.copy(
					cards = (1..20).map {
						kSomeCard.copy(name = "My Card $it")
					}
			)

			robot.interact { ctrl.updatePack(myPack) }

			robot.lookupAs<ListView<Card>>("#cardListView").let {
				expectThat(it.items).containsExactly(myPack.cards)
			}
		}
	}

	group("#UI") {
		test("Should invoke cardSelectedHandler") {
			val myCardName = (1..5).map { "My Card #$it" }

			robot.interact {
				ctrl.updatePack(kSomePack.copy(
						cards = myCardName.map { kSomeCard.copy(name = it) }
				))
			}.focus<ListView<Card>>("#cardListView")
					.type(DOWN, UP)
					.type(DOWN, 4)

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
	}

	group("#selectedCard") {
		test("Should modify selected card") {
			val myNewSelectedCard = Card(name = "my new selected card")

			robot.focus<ListView<Card>>("#cardListView")
					.type(DOWN, UP)
					.type(DOWN, 3)
					.interact { ctrl.modifySelectedCard(myNewSelectedCard) }

			robot.lookupAs<ListView<Card>>("#cardListView").let {
				expectThat(it.selectionModel.selectedIndex).isEqualTo(3)
				expectThat(it.items[3]).isEqualTo(myNewSelectedCard)
			}
		}
	}


})