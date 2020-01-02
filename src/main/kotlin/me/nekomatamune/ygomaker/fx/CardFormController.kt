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

typealias CardModifiedHandler = (Card) -> Unit

class CardFormController {

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
	@FXML lateinit var cardImageController: CardImageController

	var cardModifiedHandler: CardModifiedHandler = {}

	var onSelectCardInProgress: Boolean = false

	var card = Card()
		private set

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

		cardImageController.imageModifiedHandler = {
			card = card.copy(image = it)
			cardModifiedHandler(card)
		}
	}

	fun setState(card: Card, packDir: Path) {
		logger.info {
			"setState(card=$card, packDir=$packDir)"
		}

		this.card = card.copy()

		onSelectCardInProgress = true

		cardNameTextField.text = card.name
		cardTypeComboBox.selectionModel.select(card.type)
		attributeComboBox.selectionModel.select(card.monster?.attribute)
		levelComboBox.selectionModel.select(card.monster?.level)
		monsterTypeComboBox.selectionModel.select(card.monster?.type)
		monsterAbilityComboBox.selectionModel.select(card.monster?.ability ?: "")
		effectCheckBox.isSelected = card.monster?.effect ?: false
		effectTextArea.text = card.effect
		atkTextField.text = card.monster?.atk ?: ""
		defTextField.text = card.monster?.def ?: ""
		codeTextField.text = card.code

		cardImageController.setImage(card.image ?: Image(), packDir)

		onSelectCardInProgress = false
	}

	private fun onCardValueChange() {
		logger.trace { "onCardValueChange()" }
		if (onSelectCardInProgress) {
			return
		}

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
