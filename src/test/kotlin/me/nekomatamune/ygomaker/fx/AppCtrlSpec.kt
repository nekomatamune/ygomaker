package me.nekomatamune.ygomaker.fx

import io.mockk.every
import io.mockk.mockk
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

	val kSomePackCode = "SOME"

	beforeEachTest {
		every { mockCommand.packCode }.returns(kSomePackCode)
	}

	group("#MenuBar") {
		group("#LoadPack") {
			test("Should ask CardListCtrl to load pack") {

			}
		}
	}

})