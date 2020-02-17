package me.nekomatamune.ygomaker.fx

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import javafx.scene.input.KeyCode.DOWN
import javafx.scene.input.KeyCode.ENTER
import me.nekomatamune.ygomaker.Attribute
import me.nekomatamune.ygomaker.Card
import me.nekomatamune.ygomaker.CardType
import me.nekomatamune.ygomaker.Image
import me.nekomatamune.ygomaker.MONSTER_ABILITY_PRESETS
import me.nekomatamune.ygomaker.MONSTER_TYPE_PRESETS
import me.nekomatamune.ygomaker.Result
import me.nekomatamune.ygomaker.success
import org.spekframework.spek2.Spek
import org.testfx.api.FxRobot
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import strikt.assertions.isTrue
import java.nio.file.Paths

object CardFormCtrlSpec : Spek({
	val mockCardImage by memoized { mockk<CardImageCtrl>(relaxed = true) }
	setupTestFx<CardFormCtrl>("fx/CardForm.fxml", mapOf(
			CardImageCtrl::class to { mockCardImage },
			CardFormCtrl::class to { CardFormCtrl() }
	))

	val ctrl by memoized<CardFormCtrl>()
	val robot by memoized<FxRobot>()

	lateinit var card: Card
	val capturedImageModifiedHandler = slot<(Image) -> Result<Unit>>()
	beforeEachTest {
		every {
			mockCardImage.setImageModifiedHandler(
					capture(capturedImageModifiedHandler)
			)
		}.just(Runs)

		ctrl.setCardModifiedHandler {
			card = it
			success()
		}
	}

	group("Basic") {
		test("Should set default ComboBox values to be the first options") {
			ctrl.card.let {
				expectThat(it.type).isEqualTo(CardType.NORMAL_SUMMON_MONSTER)
				expectThat(it.monster).isNotNull().get {
					expectThat(level).isEqualTo(1)
					expectThat(attribute).isEqualTo(Attribute.LIGHT)
					expectThat(type).isEqualTo(MONSTER_TYPE_PRESETS[0])
					expectThat(ability).isEqualTo(MONSTER_ABILITY_PRESETS[0])
				}
			}
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
	}

	group("Interaction with CardImage") {
		test("Should update image when modified") {
			val image = Image(file = "my_file", x = 123, y = 456, size = 789)
			capturedImageModifiedHandler.captured(image)
			expectThat(ctrl.card.image).isEqualTo(image)
		}

		test("Should set image") {
			val myCard = Card(
					image = Image(file = "my_file", x = 123, y = 456, size = 777))
			val myPackDir = Paths.get("my_path")

			runFx {
				ctrl.setState(newCard = myCard, newPackDir = myPackDir)
			}

			verify { mockCardImage.setState(myCard.image!!, myPackDir) }
		}
	}
})
