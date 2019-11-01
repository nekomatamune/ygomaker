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

class CardForm {
	private lateinit var packDir: Path
	private var onSelectCardInProgress: Boolean = false

	@FXML private lateinit var cardNameTextField: TextField
	@FXML private lateinit var cardTypeComboBox: ComboBox<CardType>
	@FXML private lateinit var attributeComboBox: ComboBox<Attribute>
	@FXML private lateinit var levelComboBox: ComboBox<Int>
	@FXML private lateinit var monsterTypeComboBox: ComboBox<String>
	@FXML private lateinit var monsterAbilityComboBox: ComboBox<String>
	@FXML private lateinit var effectCheckBox: CheckBox
	@FXML private lateinit var effectTextArea: TextArea
	@FXML private lateinit var atkTextField: TextField
	@FXML private lateinit var defTextField: TextField
	@FXML private lateinit var codeTextField: TextField

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
			sequenceOf(cardNameTextField, cardTypeComboBox)
		).forEach {
			it.addSimpleListener { onCardValueChange() }
		}

		dispatcher.register(EventType.SELECT_CARD) { onSelectCard(it) }
	}

	private fun onCardValueChange() {
		logger.trace { "onCardValueChange()" }
		if (onSelectCardInProgress) {
			return
		}

		val newCard = Card(
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

		dispatcher.dispatch(Event(EventType.MODIFY_CARD, card = newCard))
	}

	private fun onSelectCard(event: Event): Result<Unit> {
		try {
			onSelectCardInProgress = true

			packDir = event.packDir!!

			val card = event.card!!
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

			return Result.success()

		} finally {
			onSelectCardInProgress = false
		}
	}
}
