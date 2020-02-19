package me.nekomatamune.ygomaker.fx

import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import javafx.scene.control.CheckBox
import javafx.scene.control.ComboBox
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import me.nekomatamune.ygomaker.Attribute
import me.nekomatamune.ygomaker.Card
import me.nekomatamune.ygomaker.CardType
import me.nekomatamune.ygomaker.Image
import me.nekomatamune.ygomaker.MONSTER_ABILITY_PRESETS
import me.nekomatamune.ygomaker.MONSTER_TYPE_PRESETS
import me.nekomatamune.ygomaker.Monster
import me.nekomatamune.ygomaker.Result
import org.spekframework.spek2.Spek
import org.testfx.api.FxRobot
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.nio.file.Path
import java.nio.file.Paths

object CardFormCtrlSpec : Spek({
	val kSomeCard = Card(
			name = "someCardName",
			type = CardType.XYZ_MONSTER,
			monster = Monster(
					level = 9,
					attribute = Attribute.DIVINE,
					type = MONSTER_TYPE_PRESETS[1],
					ability = MONSTER_ABILITY_PRESETS[3],
					effect = true,
					atk = "1234",
					def = "5678"
			),
			effect = "someOfMyEffect",
			code = "someCode",
			image = Image(file = "someFile")
	)
	val kSomePath = Paths.get("this", "is", "some", "path")

	val mockCardImage by memoized { mockk<CardImageCtrl>(relaxed = true) }
	setupTestFx<CardFormCtrl>("fx/CardForm.fxml", mapOf(
			CardImageCtrl::class to { mockCardImage },
			CardFormCtrl::class to { CardFormCtrl() }
	))

	val ctrl by memoized<CardFormCtrl>()
	val robot by memoized<FxRobot>()

	val mockCardModifiedHandler = mockk<(Card) -> Result<Unit>>()
	beforeEachTest {
		ctrl.cardModifiedHandler = mockCardModifiedHandler
		runFx { ctrl.setState(kSomeCard, kSomePath) }
	}

	group("setState") {
		test("Should populate components with card data") {
			val myCard = kSomeCard.copy(
					name = "myCardName",
					type = CardType.SPECIAL_SUMMON_MONSTER,
					monster = Monster(
							level = 5,
							attribute = Attribute.FIRE,
							type = MONSTER_TYPE_PRESETS[2],
							ability = MONSTER_ABILITY_PRESETS[4],
							effect = true,
							atk = "1111",
							def = "2222"
					),
					effect = "myEffect\nmyAnotherEffect",
					code = "myCode"
			)

			runFx { ctrl.setState(myCard, kSomePath) }

			robot.lookupAs<TextField>("#cardNameTextField").let {
				expectThat(it.text).isEqualTo(myCard.name)
			}
			robot.lookupAs<ComboBox<CardType>>("#cardTypeComboBox").let {
				expectThat(it.selectionModel.selectedItem).isEqualTo(myCard.type)
			}
			robot.lookupAs<ComboBox<Attribute>>("#attributeComboBox").let {
				expectThat(it.selectionModel.selectedItem)
						.isEqualTo(myCard.monster?.attribute)
			}
			robot.lookupAs<ComboBox<Int>>("#levelComboBox").let {
				expectThat(it.selectionModel.selectedItem)
						.isEqualTo(myCard.monster?.level)
			}
			robot.lookupAs<ComboBox<String>>("#monsterTypeComboBox").let {
				expectThat(it.selectionModel.selectedItem)
						.isEqualTo(myCard.monster?.type)
			}
			robot.lookupAs<ComboBox<String>>("#monsterAbilityComboBox").let {
				expectThat(it.selectionModel.selectedItem)
						.isEqualTo(myCard.monster?.ability)
			}
			robot.lookupAs<CheckBox>("#effectCheckBox").let {
				expectThat(it.isSelected).isEqualTo(myCard.monster?.effect)
			}
			robot.lookupAs<TextArea>("#effectTextArea").let {
				expectThat(it.text).isEqualTo(myCard.effect)
			}
			robot.lookupAs<TextField>("#atkTextField").let {
				expectThat(it.text).isEqualTo(myCard.monster?.atk)
			}
			robot.lookupAs<TextField>("#defTextField").let {
				expectThat(it.text).isEqualTo(myCard.monster?.def)
			}
			robot.lookupAs<TextField>("#codeTextField").let {
				expectThat(it.text).isEqualTo(myCard.code)
			}
		}

		test("Should set CardImageCtrl state") {
			val myImage = Image(file = "myFile")
			val myPackDir = Paths.get("my", "pack", "dir");

			runFx {
				ctrl.setState(kSomeCard.copy(image = myImage), myPackDir)
			}

			val actualImage = slot<Image>()
			val actualPackDir = slot<Path>()
			verify {
				mockCardImage.setState(capture(actualImage), capture(actualPackDir))
			}
			expectThat(actualImage.captured).isEqualTo(myImage)
			expectThat(actualPackDir.captured).isEqualTo(myPackDir)
		}
		//
		//		test("Should modify card name") {
		//			robot.clickOn("#cardNameTextField")
		//			robot.write("Hello World!")
		//			expectThat(card.name).isEqualTo("Hello World!")
		//		}
		//
		//		test("Should modify card type") {
		//			robot.clickOn("#cardTypeComboBox")
		//			robot.type(DOWN, CardType.SPECIAL_SUMMON_MONSTER.ordinal)
		//			robot.type(ENTER)
		//			expectThat(card.type).isEqualTo(CardType.SPECIAL_SUMMON_MONSTER)
		//		}
		//
		//		test("Should modify attribute") {
		//			robot.clickOn("#attributeComboBox")
		//			robot.type(DOWN, Attribute.WATER.ordinal)
		//			robot.type(ENTER)
		//			expectThat(card.monster?.attribute).isEqualTo(Attribute.WATER)
		//		}
		//
		//		test("Should modify level") {
		//			robot.clickOn("#levelComboBox")
		//			robot.type(DOWN, 3)
		//			robot.type(ENTER)
		//			expectThat(card.monster?.level).isEqualTo(4)
		//		}
		//
		//		test("Should modify monster type") {
		//			robot.clickOn("#monsterTypeComboBox")
		//			robot.type(DOWN, 10)
		//			robot.type(ENTER)
		//			expectThat(card.monster?.type).isEqualTo(MONSTER_TYPE_PRESETS[10])
		//		}
		//
		//		test("Should modify monster effect checker") {
		//			robot.clickOn("#effectCheckBox")
		//			expectThat(card.monster?.effect).isTrue()
		//		}
		//
		//		test("Should modify effect") {
		//			robot.clickOn("#effectTextArea")
		//			robot.write("First line.\n")
		//			robot.write("Second line.")
		//			expectThat(card.effect).isEqualTo("First line.\nSecond line.")
		//		}
		//
		//		test("Should modify atk/def") {
		//			robot.clickOn("#atkTextField")
		//			robot.write("3000")
		//			robot.clickOn("#defTextField")
		//			robot.write("2500")
		//			expectThat(card).get {
		//				expectThat(monster?.atk).isEqualTo("3000")
		//				expectThat(monster?.def).isEqualTo("2500")
		//			}
		//		}
	}

	//	group("Interaction with CardImage") {
	//		test("Should update image when modified") {
	//			val image = Image(file = "my_file", x = 123, y = 456, size = 789)
	//			cardSlot.captured(image)
	//			expectThat(ctrl.card.image).isEqualTo(image)
	//		}
	//
	//		test("Should set image") {
	//			val myCard = Card(
	//					image = Image(file = "my_file", x = 123, y = 456, size = 777))
	//			val myPackDir = Paths.get("my_path")
	//
	//			runFx {
	//				ctrl.setState(newCard = myCard, newPackDir = myPackDir)
	//			}
	//
	//			verify { mockCardImage.setState(myCard.image!!, myPackDir) }
	//		}
	//	}
})
