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

	var onSelectCardInProgress: Boolean = false

	@FXML
	fun initialize() {
		logger.debug { "Initializing CardForm" }

		cardTypeComboBox.items = observableArrayList(CardType.values().toList())
		attributeComboBox.items = observableArrayList(Attribute.values().toList())
		levelComboBox.items = observableArrayList((1..12).toList())
		monsterTypeComboBox.items = observableArrayList(MONSTER_TYPE_PRESETS)
		monsterAbilityComboBox.items = observableArrayList(MONSTER_ABILITY_PRESETS)

		cardTypeComboBox.valueProperty().addListener { _, _, newValue ->
			logger.trace { "Card type changed to $newValue" }

			(newValue in MONSTER_CARD_TYPES).let { isMonsterCard ->
				attributeComboBox.isDisable = !isMonsterCard
				levelComboBox.isDisable = !isMonsterCard
				monsterTypeComboBox.isDisable = !isMonsterCard
				monsterAbilityComboBox.isDisable = !isMonsterCard
				effectCheckBox.isDisable = !isMonsterCard
				atkTextField.isEditable = isMonsterCard
				defTextField.isEditable = isMonsterCard
			}
		}


		sequenceOf(
			cardNameTextField, atkTextField, defTextField, effectTextArea
		).forEach {
			it.textProperty().addSimpleListener(::onCardValueChange)
		}

		sequenceOf(
			cardTypeComboBox, attributeComboBox, levelComboBox,
			monsterTypeComboBox, monsterAbilityComboBox
		).forEach {
			it.valueProperty().addSimpleListener(::onCardValueChange)
		}

		effectCheckBox.selectedProperty().addSimpleListener(::onCardValueChange)

		registerEventHandler(EventName.SELECT_CARD) {
			logger.trace { "Handling SELECT_CARD event" }
			packDir = it.packDir!!
			onSelectCard(it.card!!)
			Result.success(Unit)
		}
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
				ability = monsterAbilityComboBox.value.ifEmpty { null },
				effect = effectCheckBox.isSelected,
				atk = atkTextField.text,
				def = defTextField.text
			)
		)

		dispatchEvent(Event(name = EventName.MODIFY_CARD, card = newCard))
	}

	private fun onSelectCard(card: Card) {
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

		onSelectCardInProgress = false
	}
}
