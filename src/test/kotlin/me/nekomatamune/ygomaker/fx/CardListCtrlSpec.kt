package me.nekomatamune.ygomaker.fx

import org.spekframework.spek2.Spek
import org.testfx.api.FxRobot

object CardListCtrlSpec : Spek({
	setupTestFx<CardListCtrl>("fx/CardList.fxml", mapOf(
			CardListCtrl::class to { CardListCtrl() }
	))

	val ctrl by memoized<CardListCtrl>()
	val robot by memoized<FxRobot>()

	beforeEachTest {

	}

	test("dummy test") {

	}

})