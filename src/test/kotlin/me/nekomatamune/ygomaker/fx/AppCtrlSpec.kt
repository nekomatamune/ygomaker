package me.nekomatamune.ygomaker.fx

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import javafx.scene.input.KeyCode
import me.nekomatamune.ygomaker.Card
import me.nekomatamune.ygomaker.Command
import me.nekomatamune.ygomaker.Result
import me.nekomatamune.ygomaker.success
import org.spekframework.spek2.Spek
import org.testfx.api.FxRobot
import java.nio.file.Path
import java.nio.file.Paths

private val SUPER = if (System.getProperty(
				"os.name") == "Mac OS X") KeyCode.COMMAND else KeyCode.WINDOWS

object AppCtrlSpec : Spek({
	val mockCommand = mockk<Command>(relaxed = true).also {
		every { it.dataDir }.returns(Paths.get("DATA"))
		every { it.packCode }.returns("SOME")
	}
	val cardSelectedHandlerSlot = slot<(Card, Path) -> Result<Unit>>()
	val mockCardListCtrl = mockk<CardListCtrl>(relaxed = true).also {
		every {
			it.cardSelectedHandler = capture(cardSelectedHandlerSlot)
		}.just(Runs)
	}

	val cardModifiedHandlerSlot = slot<(Card) -> Result<Unit>>()
	val mockCardFormCtrl = mockk<CardFormCtrl>(relaxed = true).also {
		every { it.cardModifiedHandler = capture(cardModifiedHandlerSlot) }.just(
				Runs)
	}
	val mockCardRendererCtrl = mockk<CardRendererController>(relaxed = true)

	setupTestFx<AppCtrl>("fx/App.fxml", mapOf(
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
				robot.press(SUPER, KeyCode.O).release(SUPER, KeyCode.O)
				verify { mockCardListCtrl.loadPack() }
			}
		}

		group("#SavePackMenuItem") {
			test("Should ask CardList to save pack") {
				robot.press(SUPER, KeyCode.S).release(SUPER, KeyCode.S)
				every { mockCardListCtrl.savePack() }
			}
		}

		group("#SavePackAsMenuItem") {
			test("Should ask CardList to save pack as") {
				robot.press(SUPER, KeyCode.SHIFT, KeyCode.S).release(SUPER,
						KeyCode.SHIFT, KeyCode.S)
				every { mockCardListCtrl.savePackAs() }
			}
		}

		group("#NewCardMenuItem") {
			test("Should ask CardList to add new card") {
				robot.press(SUPER, KeyCode.N).release(SUPER, KeyCode.N)
				every { mockCardListCtrl.addCard() }
			}
		}

		group("#ExitMenuItem") {
			test("Should not exit if cancelled") {
				// TODO
			}
		}


	}

	group("#CardList") {
		group("#cardSelectedHandler") {
			test("Should update CardForm and CardRenderer") {
				val myCard = Card(name = "MyNewCard")
				val myPackDir = Paths.get("MyNewPackDir")
				every { mockCardRendererCtrl.render(any(), any()) }.returns(success())

				robot.interact {
					cardSelectedHandlerSlot.captured(myCard, myPackDir).assertSuccess()
				}

				verify { mockCardFormCtrl.setState(myCard, myPackDir) }
				verify { mockCardRendererCtrl.render(myCard, myPackDir) }
			}
		}
	}

	group("#CardForm") {
		group("#cardModifiedHandler") {
			test("Should update CardList only") {
				val myCard = Card(name = "MyModifiedCard")
				robot.interact {
					cardModifiedHandlerSlot.captured(myCard).assertSuccess()
				}

				verify { mockCardListCtrl.modifySelectedCard(myCard) }
				verify(exactly = 0) { mockCardRendererCtrl.render(myCard, any()) }
			}
		}
	}
})

