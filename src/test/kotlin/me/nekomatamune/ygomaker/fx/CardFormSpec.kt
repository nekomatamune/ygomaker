package me.nekomatamune.ygomaker.fx

import com.google.common.io.Resources
import io.mockk.*
import javafx.application.Application
import javafx.application.Platform
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.input.KeyCode.DOWN
import javafx.scene.input.KeyCode.ENTER
import javafx.scene.layout.GridPane
import javafx.stage.Stage
import javafx.util.Callback
import me.nekomatamune.ygomaker.*
import org.spekframework.spek2.Spek
import org.testfx.api.FxRobot
import org.testfx.api.FxToolkit
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isTrue

object CardFormSpec : Spek({
	val robot = FxRobot()
	lateinit var app: Application
	lateinit var ctrl: CardForm
	lateinit var pane: GridPane

	lateinit var card: Card

	var imageModifiedHandler = slot<ImageModifiedHandler>()
	val mockCardImage = mockk<CardImage>(relaxed = true)

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
		every {
			mockCardImage.imageModifiedHandler = capture(imageModifiedHandler)
		}.just(Runs)

		app = FxToolkit.setupApplication {
			object : Application() {
				override fun start(primaryStage: Stage) {
					FXMLLoader().apply {
						location = Resources.getResource("fx/CardForm.fxml")

						controllerFactory = Callback<Class<*>, Any> {
							when (it) {
								CardImage::class.java -> mockCardImage
								CardForm::class.java -> CardForm()
								else -> throw UnsupportedOperationException(it.toString())
							}
						}
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
		robot.clickOn("#cardNameTextField")
		robot.write("Hello World!")
		expectThat(card.name).isEqualTo("Hello World!")
	}

	test("Should modify card type") {
		robot.clickOn("#cardTypeComboBox")
		robot.type(DOWN, CardType.SPECIAL_SUMMON_MONSTER.ordinal)
		robot.type(ENTER)
		expectThat(card.type).isEqualTo(CardType.SPECIAL_SUMMON_MONSTER)
	}

	test("Should modify attribute") {
		robot.clickOn("#attributeComboBox")
		robot.type(DOWN, Attribute.WATER.ordinal)
		robot.type(ENTER)
		expectThat(card.monster?.attribute).isEqualTo(Attribute.WATER)
	}

	test("Should modify level") {
		robot.clickOn("#levelComboBox")
		robot.type(DOWN, 3)
		robot.type(ENTER)
		expectThat(card.monster?.level).isEqualTo(4)
	}

	test("Should modify monster type") {
		robot.clickOn("#monsterTypeComboBox")
		robot.type(DOWN, 10)
		robot.type(ENTER)
		expectThat(card.monster?.type).isEqualTo(MONSTER_TYPE_PRESETS[10])
	}

	test("Should modify monster effect checker") {
		robot.clickOn("#effectCheckBox")
		expectThat(card.monster?.effect).isTrue()
	}

	test("Should modify effect") {
		robot.clickOn("#effectTextArea")
		robot.write("First line.\n")
		robot.write("Second line.")
		expectThat(card.effect).isEqualTo("First line.\nSecond line.")
	}

	test("Should modify atk/def") {
		robot.clickOn("#atkTextField")
		robot.write("3000")
		robot.clickOn("#defTextField")
		robot.write("2500")
		expectThat(card).get {
			expectThat(monster?.atk).isEqualTo("3000")
			expectThat(monster?.def).isEqualTo("2500")
		}
	}

	test("Should update image when modified") {
		val image = Image(file = "my_file", x = 123, y = 456, size = 789)
		imageModifiedHandler.invoke(image)
		expectThat(card.image).isEqualTo(image)
	}

})
