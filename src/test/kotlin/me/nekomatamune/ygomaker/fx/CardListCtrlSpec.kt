package me.nekomatamune.ygomaker.fx

import javafx.scene.control.ListView
import me.nekomatamune.ygomaker.Card
import me.nekomatamune.ygomaker.Language
import me.nekomatamune.ygomaker.Pack
import org.spekframework.spek2.Spek
import org.testfx.api.FxRobot
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.isEqualTo

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

	beforeEachTest {
		runFx {
			ctrl.setState(kSomePack)
		}
	}

	group("#setState") {
		test("Should populate card list") {
			val myPack = kSomePack.copy(
					cards = (1..10).map {
						kSomeCard.copy(name = "My Card $it")
					}
			)

			runFx { ctrl.setState(myPack) }

			robot.lookupAs<ListView<Card>>("#cardListView").let {
				expectThat(it.items).containsExactly(myPack.cards)
			}
		}
	}

})