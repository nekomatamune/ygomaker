package me.nekomatamune.ygomaker.fx

import javafx.collections.FXCollections.observableArrayList
import javafx.fxml.FXML
import javafx.scene.control.CheckBox
import javafx.scene.control.ComboBox
import javafx.scene.control.ComboBoxBase
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import javafx.scene.control.TextInputControl
import me.nekomatamune.ygomaker.Attribute
import me.nekomatamune.ygomaker.Card
import me.nekomatamune.ygomaker.CardType
import me.nekomatamune.ygomaker.Image
import me.nekomatamune.ygomaker.MONSTER_ABILITY_PRESETS
import me.nekomatamune.ygomaker.MONSTER_LEVEL_PRESETS
import me.nekomatamune.ygomaker.MONSTER_TYPE_PRESETS
import me.nekomatamune.ygomaker.Monster
import me.nekomatamune.ygomaker.Result
import me.nekomatamune.ygomaker.failure
import me.nekomatamune.ygomaker.isMonster
import me.nekomatamune.ygomaker.success
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

	private val monsterOnlyFields by lazy {
		listOf(
				attributeComboBox, levelComboBox,
				monsterTypeComboBox, monsterAbilityComboBox, effectCheckBox,
				atkTextField, defTextField)
	}

	// region Controller states
	private lateinit var packDir: Path
	private lateinit var image: Image
	private var card: Card
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
				image = if (image.file.isBlank()) null else image
		)
		set(value) {
			handlerLock.lockAndRun {
				monsterOnlyFields.forEach {
					it.isDisable = !value.type.isMonster()
				}

				cardNameTextField.text = value.name
				cardTypeComboBox.selectionModel.select(value.type)
				effectTextArea.text = value.effect
				codeTextField.text = value.code

				attributeComboBox.selectionModel.select(value.monster?.attribute)
				levelComboBox.selectionModel.select(value.monster?.level)
				monsterTypeComboBox.selectionModel.select(value.monster?.type)
				monsterAbilityComboBox.selectionModel.select(
						value.monster?.ability ?: "")
				effectCheckBox.isSelected = value.monster?.effect ?: false
				atkTextField.text = value.monster?.atk ?: ""
				defTextField.text = value.monster?.def ?: ""
			}

			(value.image ?: Image()).let {
				image = it
				cardImageController.setState(it, packDir)
			}
		}

	private val handlerLock = HandlerLock()
	var cardModifiedHandler: (Card) -> Result<Unit> = {
		failure(IllegalStateException("Handler not set"))
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

		// Group components together
		val allFields = listOf(
				cardNameTextField, cardTypeComboBox, attributeComboBox, levelComboBox,
				monsterTypeComboBox, monsterAbilityComboBox, effectCheckBox,
				effectTextArea, atkTextField, defTextField, codeTextField
		)

		val allComboBoxes = mapOf(
				cardTypeComboBox to CardType.values().toList(),
				attributeComboBox to Attribute.values().toList(),
				levelComboBox to MONSTER_LEVEL_PRESETS,
				monsterTypeComboBox to MONSTER_TYPE_PRESETS,
				monsterAbilityComboBox to MONSTER_ABILITY_PRESETS
		)

		// Set selectable values for ComboBoxes and select 1st value
		allComboBoxes.forEach {
			it.key.items = observableArrayList(it.value)
			it.key.selectionModel.selectFirst()
		}


		// Special rules to apply when CardType changes
		cardTypeComboBox.valueProperty().addListener(
				handlerLock) { oldCardType, newCardType ->
			logger.trace { "Card type changed from $oldCardType to $newCardType" }

			handlerLock.lockAndRun {
				monsterOnlyFields.forEach {
					if (newCardType.isMonster()) {
						it.isDisable = false

					} else {
						it.isDisable = true
						when (it) {
							is ComboBox<*> -> it.selectionModel.selectFirst()
							is TextField -> it.text = ""
							is CheckBox -> it.isSelected = false
						}
					}
				}
			}
		}

		// Capture the image when it gets modified
		cardImageController.imageModifiedHandler = {
			image = it
			success()
		}

		// Set FXML component listeners
		allFields.forEach {
			when (it) {
				is TextInputControl -> it.textProperty()
				is ComboBoxBase<*> -> it.valueProperty()
				is CheckBox -> it.selectedProperty()
				else -> error("Unexpected component class ${it::class}")
			}.addListener(handlerLock) { _, _ ->
				cardModifiedHandler(card).alertFailure()
			}
		}
	}

	fun setState(newCard: Card, newPackDir: Path = packDir) {
		logger.info {
			"setState(card=$newCard, packDir=$newPackDir)"
		}

		packDir = newPackDir
		card = newCard
	}
}
