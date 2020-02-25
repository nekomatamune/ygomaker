package me.nekomatamune.ygomaker.fx

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import javafx.scene.input.KeyCode
import me.nekomatamune.ygomaker.Command
import org.spekframework.spek2.Spek
import org.testfx.api.FxRobot
import java.nio.file.Paths

private val SUPER = if (System.getProperty(
				"os.name") == "Mac OS X") KeyCode.COMMAND else KeyCode.WINDOWS

object AppCtrlSpec : Spek({
	val mockCommand = mockk<Command>(relaxed = true).also {
		every { it.dataDir }.returns(Paths.get("DATA"))
		every { it.packCode }.returns("SOME")
	}
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

	group("#init") {
		test("Should load CardList with initial packDir") {
			val expectedInitialPackDir = Paths.get("DATA", "SOME")
			verify { mockCardListCtrl.loadPack(expectedInitialPackDir) }
		}
	}

	group("#MenuBar") {
		group("#LoadPackMenuItem") {
			test("Should ask CardList to load pack") {
				robot.press(SUPER, KeyCode.O)
				verify { mockCardListCtrl.loadPack() }
			}
		}

		group("#SavePackMenuItem") {
			test("Should ask CardList to save pack") {
				robot.press(SUPER, KeyCode.S)
				every { mockCardListCtrl.savePack() }
			}
		}

		group("#SavePackAsMenuItem") {
			test("Should ask CardList to save pack as") {
				robot.press(SUPER, KeyCode.SHIFT, KeyCode.S)
				every { mockCardListCtrl.savePackAs() }
			}
		}

		group("#NewCardMenuItem") {
			test("Should ask CardList to add new card") {
				robot.press(SUPER, KeyCode.N)
				every { mockCardListCtrl.addCard() }
			}
		}

		group("#ExitMenuItem") {
			test("Should not exit if cancelled") {
				// TODO
			}
		}



	}

})

