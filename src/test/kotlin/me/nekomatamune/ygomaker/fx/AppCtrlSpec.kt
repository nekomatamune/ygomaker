package me.nekomatamune.ygomaker.fx

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import javafx.scene.input.KeyCode
import me.nekomatamune.ygomaker.Command
import org.spekframework.spek2.Spek
import org.testfx.api.FxRobot

object AppCtrlSpec : Spek({
	val mockCommand = mockk<Command>(relaxed = true)
	val mockCardListCtrl = mockk<CardListCtrl>(relaxed = true)
	val mockCardFormCtrl = mockk<CardFormCtrl>(relaxed = true)
	val mockCardRendererCtrl = mockk<CardRendererController>(relaxed = true)

	setupTestFx<CardFormCtrl>("fx/App.fxml", mapOf(
			CardListCtrl::class to { mockCardListCtrl },
			CardFormCtrl::class to { mockCardFormCtrl },
			CardRendererController::class to { mockCardRendererCtrl },
			AppCtrl::class to { AppCtrl(cmd = mockCommand) },
			// TODO: find a way to not have to define controllers not visible
			CardImageCtrl::class to { CardImageCtrl() }
	))
	val ctrl by memoized<AppCtrl>()
	val robot by memoized<FxRobot>()

	beforeEachTest {
		every { mockCommand.packCode }.returns("SOME")
	}

	group("#MenuBar") {
		group("#LoadPackMenuItem") {
			test("Should ask CardList to load pack") {
				robot.press(KeyCode.COMMAND, KeyCode.O)
				verify { mockCardListCtrl.loadPack(null) }
			}
		}

		group("#SavePackMenuItem") {
			test("Should ask CardList to save pack") {
				robot.press(KeyCode.COMMAND, KeyCode.S)
				every { mockCardListCtrl.savePack() }
			}
		}

		group("#SavePackAsMenuItem") {
			test("Should ask CardList to save pack as") {
				robot.press(KeyCode.COMMAND, KeyCode.SHIFT, KeyCode.S)
				every { mockCardListCtrl.savePackAs() }
			}
		}

		group("#NewCardMenuItem") {
			test("Should ask CardList to add new card") {
				robot.press(KeyCode.COMMAND, KeyCode.N)
				every { mockCardListCtrl.addCard() }
			}
		}
	}

})