package me.nekomatamune.ygomaker.fx

import javafx.collections.FXCollections.observableArrayList
import javafx.fxml.FXML
import javafx.scene.control.CheckBox
import javafx.scene.control.ComboBox
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import me.nekomatamune.ygomaker.*
import mu.KotlinLogging
import java.nio.file.Path

private val logger = KotlinLogging.logger { }

open class CardFormCtrl {

	// region FXML components
	@FXML lateinit var cardNameTextField: TextField
	@FXML lateinit var cardTypeComboBox: ComboBox<CardType>
	@FXML lateinit var attributeComboBox: ComboBox<Attribute>
	@FXML lateinit var levelComboBox: ComboBox<Int>
	@FXML lateinit var monsterTypeComboBox: ComboBox<String>
	@FXML lateinit var monsterAbilityComboBox: ComboBox<String>
	@FXML lateinit var effectCheckBox: CheckBox
	@FXML lateinit var effectTextArea: TextArea
	@FXML lateinit var atkTextField: TextField
	@FXML lateinit var defTextField: TextField
	@FXML lateinit var codeTextField: TextField
	@FXML lateinit var cardImageController: CardImageCtrl
	// endregion

	// region Controller states
	private var card = Card()
	private var cardModifiedHandler: (Card) -> Result<Unit> = {
		failure(IllegalStateException("Handler not set!"))
	}

	private val cardFieldsListenerLock = SoftLock()
	// endregion


	@FXML
	fun initialize() {
		logger.debug { "Initializing CardForm" }

		cardTypeComboBox.items = observableArrayList(CardType.values().toList())
		attributeComboBox.items = observableArrayList(Attribute.values().toList())
		levelComboBox.items = observableArrayList((1..12).toList())
		monsterTypeComboBox.items = observableArrayList(MONSTER_TYPE_PRESETS)
		monsterAbilityComboBox.items = observableArrayList(MONSTER_ABILITY_PRESETS)

		// Group fields together for ease of reference later
		val monsterComboBoxes = sequenceOf(
				attributeComboBox, levelComboBox,
				monsterTypeComboBox, monsterAbilityComboBox
		)
		val monsterFields = monsterComboBoxes.plus(
				sequenceOf(effectCheckBox, atkTextField, defTextField)
		)

		// Set the first value to be the default
		monsterComboBoxes.plus(cardTypeComboBox).forEach {
			it.selectionModel.selectFirst()
		}

		// Special rules to apply when CardType changes
		cardTypeComboBox.valueProperty().addListener { _, oldCardType, newCardType ->
			logger.trace { "Card type changed from $oldCardType to $newCardType" }

			monsterFields.forEach {
				it.isDisable = !newCardType.isMonster()
			}

			if (!oldCardType.isMonster() && newCardType.isMonster()) {
				monsterComboBoxes.forEach { it.selectionModel.selectFirst() }
			}
		}

		monsterFields.plus(
				sequenceOf(cardNameTextField, cardTypeComboBox, effectTextArea,
						codeTextField)
		).forEach {
			it.addSimpleListener { onCardValueChange() }
		}

		cardImageController.setImageModifiedHandler {
			card = card.copy(image = it)
			cardModifiedHandler(card)

			logger.info { "after cardModifiedHandler" }

			//TODO: return the correct result
			success()
		}
	}

	// region simple getter/setter
	fun card() = card

	fun setCardModifiedHandler(handler: (Card) -> Result<Unit>) {
		cardModifiedHandler = handler
	}
	// endregion

	fun setState(newCard: Card, newPackDir: Path) {
		logger.info {
			"setState(card=$newCard, packDir=$newPackDir)"
		}

		card = newCard.copy()

		cardFieldsListenerLock.lockAndRun {
			cardNameTextField.text = newCard.name
			cardTypeComboBox.selectionModel.select(newCard.type)
			attributeComboBox.selectionModel.select(newCard.monster?.attribute)
			levelComboBox.selectionModel.select(newCard.monster?.level)
			monsterTypeComboBox.selectionModel.select(newCard.monster?.type)
			monsterAbilityComboBox.selectionModel.select(
					newCard.monster?.ability ?: "")
			effectCheckBox.isSelected = newCard.monster?.effect ?: false
			effectTextArea.text = newCard.effect
			atkTextField.text = newCard.monster?.atk ?: ""
			defTextField.text = newCard.monster?.def ?: ""
			codeTextField.text = newCard.code

			cardImageController.setState(newCard.image ?: Image(), newPackDir)
		}
	}


	private fun onCardValueChange() {
		logger.trace { "onCardValueChange()" }

		cardFieldsListenerLock.runIfNotLocked {
			card = card.copy(
					name = cardNameTextField.text,
					type = cardTypeComboBox.value,
					code = codeTextField.text,
					effect = effectTextArea.text,
					monster = if (!cardTypeComboBox.value.isMonster()) null else Monster(
							attribute = attributeComboBox.value,
							level = levelComboBox.value,
							type = monsterTypeComboBox.value,
							ability = monsterAbilityComboBox.value,
							effect = effectCheckBox.isSelected,
							atk = atkTextField.text,
							def = defTextField.text
					)
			)
			cardModifiedHandler(card)
		}
	}
}
