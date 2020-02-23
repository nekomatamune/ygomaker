package me.nekomatamune.ygomaker.fx

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import javafx.scene.control.ListView
import javafx.scene.input.KeyCode.DOWN
import javafx.scene.input.KeyCode.UP
import javafx.stage.FileChooser
import me.nekomatamune.ygomaker.Card
import me.nekomatamune.ygomaker.FileIO
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
import java.nio.file.Path
import java.nio.file.Paths

object CardListCtrlSpec : Spek({
	val mockFileChooser = mockk<FileChooser>(relaxed = true)
	val mockFileIO = mockk<FileIO>(relaxed = true)

	setupTestFx<CardListCtrl>("fx/CardList.fxml", mapOf(
			CardListCtrl::class to {
				CardListCtrl(
						fileChooserFactory = { mockFileChooser },
						fileIO = mockFileIO)
			}
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

	val mockCardSelectedHandler = mockk<(Card, Path) -> Result<Unit>>()

	val cardSlots = mutableListOf<Card>()
	val packSlot = slot<Pack>()
	val packDirSlot = slot<Path>()

	beforeEachTest {
		every { mockCardSelectedHandler(any(), any()) }.returns(success())
		every { mockFileIO.writePack(any(), any()) }.returns(success())
		ctrl.cardSelectedHandler = mockCardSelectedHandler

		robot.interact {
			ctrl.updatePackDir(Paths.get("some", "pack", "dir"))
			ctrl.updatePack(kSomePack)
		}
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

			verify(exactly = 6) {
				mockCardSelectedHandler(capture(cardSlots), any())
			}
			expectThat(cardSlots).map { it.name }.containsExactly(
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

	group("#loadPack") {
		test("Should load pack from a directory") {
			val myDataDir = Paths.get("MyDataDir")
			val myPackDir = Paths.get("my", "load", "pack", "dir")
			val myFullPackDir = myDataDir.resolve(myPackDir)
			val myPack = Pack(name = "my loaded pack", cards = listOf(
					Card(name = "my loaded card 1"),
					Card(name = "my loaded card 2")
			))

			every {
				mockFileChooser.showOpenDialog(any())
			}.returns(myFullPackDir.toFile())
			every { mockFileIO.readPack(any()) }.returns(success(myPack))
			every {
				mockCardSelectedHandler(any(), any())
			}.returns(success())

			robot.interact {
				ctrl.loadPack(dataDir = myDataDir).assertSuccess()
			}

			verify { mockFileChooser.showOpenDialog(any()) }
			verify { mockFileIO.readPack(myFullPackDir) }
			verify { mockCardSelectedHandler(myPack.cards.first(), myPackDir) }


		}
	}

	group("#savePack") {
		test("Should save pack to directory") {
			val myPackDir = Paths.get("my", "pack", "dir")
			val myPack = Pack(
					name = "my pack", cards = listOf(Card(name = "my card name"
			)))

			robot.interact {
				ctrl.updatePackDir(myPackDir)
				ctrl.updatePack(myPack)
				ctrl.savePack().assertSuccess()
			}

			verify { mockFileIO.writePack(capture(packSlot), capture(packDirSlot)) }

			expectThat(packSlot.captured).isEqualTo(myPack)
			expectThat(packDirSlot.captured).isEqualTo(myPackDir)
		}
	}


})