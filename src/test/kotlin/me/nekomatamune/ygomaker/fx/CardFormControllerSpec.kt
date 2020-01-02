package me.nekomatamune.ygomaker.fx

import com.google.common.io.Resources
import javafx.application.Application
import javafx.application.Platform
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.input.KeyCode.DOWN
import javafx.scene.input.KeyCode.ENTER
import javafx.scene.layout.GridPane
import javafx.stage.Stage
import me.nekomatamune.ygomaker.Attribute
import me.nekomatamune.ygomaker.Card
import me.nekomatamune.ygomaker.CardType
import me.nekomatamune.ygomaker.MONSTER_TYPE_PRESETS
import org.spekframework.spek2.Spek
import org.testfx.api.FxRobot
import org.testfx.api.FxToolkit
import strikt.api.expectThat
import strikt.assertions.isEqualTo


object CardFormControllerSpec : Spek({
	val robot = FxRobot()
	lateinit var app: Application
	lateinit var ctrl: CardFormCtrl
	lateinit var pane: GridPane

	lateinit var card: Card

	beforeGroup {
		FxToolkit.registerPrimaryStage()
	}

	afterGroup {
		FxToolkit.cleanupStages()
		if (FxToolkit.isFXApplicationThreadRunning()) {
			Platform.exit()
		}
	}

	beforeEachTest {
		app = FxToolkit.setupApplication {
			object : Application() {
				override fun start(primaryStage: Stage) {
					FXMLLoader().apply {
						location = Resources.getResource("fx/CardForm.fxml")
					}.let {
						pane = it.load()
						ctrl = it.getController()
					}

					primaryStage.scene = Scene(pane)
					primaryStage.show()
				}
			}
		}

		card = ctrl.card
		ctrl.cardModifiedHandler = {
			card = it.copy()
		}
	}

	afterEachTest {
		FxToolkit.cleanupApplication(app)
	}

	test("Should modify card name") {
		robot.rightClickOn("#cardNameTextField")
		robot.write("Hello World!")
		expectThat(card.name).isEqualTo("Hello World!")
	}

	test("Should modify card type") {
		robot.rightClickOn("#cardTypeComboBox")
		robot.type(DOWN, CardType.SPECIAL_SUMMON_MONSTER.ordinal)
		robot.type(ENTER)
		expectThat(card.type).isEqualTo(CardType.SPECIAL_SUMMON_MONSTER)
	}

	test("Should modify attribute") {
		robot.rightClickOn("#attributeComboBox")
		robot.type(DOWN, Attribute.WATER.ordinal)
		robot.type(ENTER)
		expectThat(card.monster?.attribute).isEqualTo(Attribute.WATER)
	}

	test("Should modify level") {
		robot.rightClickOn("#levelComboBox")
		robot.type(DOWN, 3)
		robot.type(ENTER)
		expectThat(card.monster?.level).isEqualTo(4)
	}

	test("Should modify monster type") {
		robot.rightClickOn("#monsterTypeComboBox")
		robot.type(DOWN, 10)
		robot.type(ENTER)
		expectThat(card.monster?.type).isEqualTo(MONSTER_TYPE_PRESETS[10])
	}

})
