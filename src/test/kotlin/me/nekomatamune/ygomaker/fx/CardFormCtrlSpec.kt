package me.nekomatamune.ygomaker.fx

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import javafx.scene.control.CheckBox
import javafx.scene.control.ComboBox
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import javafx.scene.input.KeyCode.DOWN
import javafx.scene.input.KeyCode.ENTER
import javafx.scene.input.KeyCode.UP
import me.nekomatamune.ygomaker.Attribute
import me.nekomatamune.ygomaker.Card
import me.nekomatamune.ygomaker.CardType
import me.nekomatamune.ygomaker.Image
import me.nekomatamune.ygomaker.MONSTER_ABILITY_PRESETS
import me.nekomatamune.ygomaker.MONSTER_LEVEL_PRESETS
import me.nekomatamune.ygomaker.MONSTER_TYPE_PRESETS
import me.nekomatamune.ygomaker.Monster
import me.nekomatamune.ygomaker.Result
import me.nekomatamune.ygomaker.success
import org.spekframework.spek2.Spek
import org.testfx.api.FxRobot
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isNotEqualTo
import strikt.assertions.isNull
import java.nio.file.Paths

object CardFormCtrlSpec : Spek({

	// Mock FX component
	val imageModifiedHandlerSlot = slot<(Image) -> Result<Unit>>()
	val mockCardImage by memoized {
		mockk<CardImageCtrl>(relaxed = true).also {
			every {
				it.imageModifiedHandler = capture(imageModifiedHandlerSlot)
			}.just(Runs)
		}
	}


	// FX test setup
	setupTestFx<CardFormCtrl>("fx/CardForm.fxml", mapOf(
			CardImageCtrl::class to { mockCardImage },
			CardFormCtrl::class to { CardFormCtrl() }
	))
	val ctrl by memoized<CardFormCtrl>()
	val robot by memoized<FxRobot>()

	// Test data
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

	// Mocks
	val mockCardModifiedHandler = mockk<(Card) -> Result<Unit>>()
	val cardSlot = slot<Card>()

	beforeEachTest {
		every { mockCardModifiedHandler(any()) }
				.returns(success())
		ctrl.cardModifiedHandler = mockCardModifiedHandler

		runFx { ctrl.setState(kSomeCard, kSomePath) }
	}

	group("#setState") {
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

			verify {
				mockCardImage.setState(myImage, myPackDir)
			}
		}
	}

	group("#UI Event") {

		test("Should modify text components") {
			val myCardName = "myCardName"
			val myEffect = "my effect\nanother my effect"
			val myCode = "TEST-MY123"

			robot.doubleClickOn("#cardNameTextField").write(myCardName)
			robot.doubleClickOn("#effectTextArea").write(myEffect)
			robot.doubleClickOn("#codeTextField").write(myCode)

			verify { mockCardModifiedHandler(capture(cardSlot)) }
			cardSlot.captured.let {
				expectThat(it.name).isEqualTo(myCardName)
				expectThat(it.effect).isEqualTo(myEffect)
				expectThat(it.code).isEqualTo(myCode)
			}
		}

		test("Should disable monster fields when card type is not monster") {
			runFx { ctrl.setState(kSomeCard.copy(type = CardType.NORMAL_TRAP)) }

			robot.doubleClickOn("#atkTextField").write("0000")
			robot.doubleClickOn("#defTextField").write("0000")

			verify { mockCardModifiedHandler(capture(cardSlot)) }

			expectThat(cardSlot.captured.monster).isNull()
		}

		test("Should reset monster fields after selecting non-monster card type") {
			runFx {
				ctrl.setState(kSomeCard.copy(
						type = CardType.SPECIAL_SUMMON_MONSTER,
						monster = Monster(
								attribute = Attribute.FIRE,
								level = 11,
								type = MONSTER_TYPE_PRESETS[2],
								ability = MONSTER_ABILITY_PRESETS[2],
								effect = true,
								atk = "3333",
								def = "5555"
						)
				))
			}

			robot.clickOn("#cardTypeComboBox")
					.type(UP, CardType.values().size)
					.type(DOWN, CardType.EQUIP_SPELL.ordinal)
					.type(ENTER)

			robot.clickOn("#cardTypeComboBox")
					.type(UP, CardType.values().size)
					.type(DOWN, CardType.FUSION_MONSTER.ordinal)
					.type(ENTER)

			verify { mockCardModifiedHandler(capture(cardSlot)) }

			cardSlot.captured.monster!!.let {
				expectThat(it.level).isEqualTo(MONSTER_LEVEL_PRESETS.first())
				expectThat(it.attribute).isEqualTo(Attribute.values().first())
				expectThat(it.type).isEqualTo(MONSTER_TYPE_PRESETS.first())
				expectThat(it.ability).isEqualTo(MONSTER_ABILITY_PRESETS.first())
				expectThat(it.effect).isFalse()
			}
		}

		group("#MonsterCardType") {
			beforeEachTest {
				runFx {
					ctrl.setState(kSomeCard.copy(type = CardType.NORMAL_SUMMON_MONSTER))
				}
			}

			test("Should modify combo box components") {
				val myLevelIdx = 3
				val myAttributeIdx = 2
				val myMonsterTypeIdx = 5
				val myMonsterAbilityIdx = 4

				robot.clickOn("#levelComboBox")
						.type(UP, MONSTER_LEVEL_PRESETS.size)
						.type(DOWN, myLevelIdx)
						.type(ENTER)
				robot.clickOn("#attributeComboBox")
						.type(UP, Attribute.values().size)
						.type(DOWN, myAttributeIdx)
						.type(ENTER)
				robot.clickOn("#monsterTypeComboBox")
						.type(UP, MONSTER_TYPE_PRESETS.size)
						.type(DOWN, myMonsterTypeIdx)
						.type(ENTER)
				robot.clickOn("#monsterAbilityComboBox")
						.type(UP, MONSTER_ABILITY_PRESETS.size)
						.type(DOWN, myMonsterAbilityIdx)
						.type(ENTER)

				verify { mockCardModifiedHandler(capture(cardSlot)) }
				cardSlot.captured.monster!!.let {
					expectThat(it.level)
							.isEqualTo(MONSTER_LEVEL_PRESETS[myLevelIdx])
					expectThat(it.attribute)
							.isEqualTo(Attribute.values()[myAttributeIdx])
					expectThat(it.type)
							.isEqualTo(MONSTER_TYPE_PRESETS[myMonsterTypeIdx])
					expectThat(it.ability)
							.isEqualTo(MONSTER_ABILITY_PRESETS[myMonsterAbilityIdx])
				}
			}

			test("Should toggle monster effect checkbox") {
				robot.clickOn("#effectCheckBox")
				verify { mockCardModifiedHandler(capture(cardSlot)) }
				val firstIsSelected = cardSlot.captured.monster!!.effect

				robot.clickOn("#effectCheckBox")
				verify { mockCardModifiedHandler(capture(cardSlot)) }
				val secondIsSelected = cardSlot.captured.monster!!.effect

				expectThat(firstIsSelected).isNotEqualTo(secondIsSelected)
			}

			test("Should modify ATK and DEF") {
				val myAtk = "3000"
				val myDef = "2500"

				robot.doubleClickOn("#atkTextField").write(myAtk)
				robot.doubleClickOn("#defTextField").write(myDef)

				verify { mockCardModifiedHandler(capture(cardSlot)) }

				cardSlot.captured.monster!!.let {
					expectThat(it.atk).isEqualTo(myAtk)
					expectThat(it.def).isEqualTo(myDef)
				}

			}
		}
	}

	test("Should handle image update from CardImageCtrl") {
		val myImage = Image(file = "myFile.jpg")

		runFx {
			imageModifiedHandlerSlot.captured(myImage)
		}

		verify { mockCardModifiedHandler(capture(cardSlot)) }

		expectThat(cardSlot.captured.image).isEqualTo(myImage)

	}
})
