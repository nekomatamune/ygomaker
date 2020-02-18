package me.nekomatamune.ygomaker.fx

import javafx.collections.FXCollections.observableArrayList
import javafx.fxml.FXML
import javafx.scene.control.CheckBox
import javafx.scene.control.ComboBox
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import me.nekomatamune.ygomaker.Attribute
import me.nekomatamune.ygomaker.Card
import me.nekomatamune.ygomaker.CardType
import me.nekomatamune.ygomaker.Image
import me.nekomatamune.ygomaker.MONSTER_ABILITY_PRESETS
import me.nekomatamune.ygomaker.MONSTER_LEVEL_PRESETS
import me.nekomatamune.ygomaker.MONSTER_TYPE_PRESETS
import me.nekomatamune.ygomaker.Monster
import me.nekomatamune.ygomaker.isMonster
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
	private lateinit var packDir: Path
	var card: Card
		get() = Card(
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
				),
				image = cardImageController.image.let {
					if (it.file.isBlank()) null else it
				}
		)
		set(value) {
			cardNameTextField.text = value.name
			cardTypeComboBox.selectionModel.select(value.type)
			attributeComboBox.selectionModel.select(value.monster?.attribute)
			levelComboBox.selectionModel.select(value.monster?.level)
			monsterTypeComboBox.selectionModel.select(value.monster?.type)
			monsterAbilityComboBox.selectionModel.select(
					value.monster?.ability ?: "")
			effectCheckBox.isSelected = value.monster?.effect ?: false
			effectTextArea.text = value.effect
			atkTextField.text = value.monster?.atk ?: ""
			defTextField.text = value.monster?.def ?: ""
			codeTextField.text = value.code

			cardImageController.setState(value.image ?: Image(), packDir)
		}
	// endregion


	/**
	 * Called by the javafx framework when this component is first loaded.
	 *
	 * Sets up listeners for own FXML components.
	 */
	@FXML
	fun initialize() {
		logger.debug { "Initializing CardForm" }

		// Set selectable values for ComboBoxes
		cardTypeComboBox.items = observableArrayList(CardType.values().toList())
		attributeComboBox.items = observableArrayList(Attribute.values().toList())
		levelComboBox.items = observableArrayList(MONSTER_LEVEL_PRESETS)
		monsterTypeComboBox.items = observableArrayList(MONSTER_TYPE_PRESETS)
		monsterAbilityComboBox.items = observableArrayList(MONSTER_ABILITY_PRESETS)

		// Set 1st value as the default value for ComboBoxes
		sequenceOf(
				cardTypeComboBox, attributeComboBox, levelComboBox,
				monsterTypeComboBox, monsterAbilityComboBox
		).forEach {
			it.selectionModel.selectFirst()
		}

		// Group fields together for ease of reference later
		val monsterComboBoxes = sequenceOf(
				attributeComboBox, levelComboBox,
				monsterTypeComboBox, monsterAbilityComboBox
		)
		val monsterFields = monsterComboBoxes.plus(
				sequenceOf(effectCheckBox, atkTextField, defTextField)
		)

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
	}

	fun setState(newCard: Card, newPackDir: Path) {
		logger.info {
			"setState(card=$newCard, packDir=$newPackDir)"
		}

		packDir = newPackDir
		card = newCard
	}
}
