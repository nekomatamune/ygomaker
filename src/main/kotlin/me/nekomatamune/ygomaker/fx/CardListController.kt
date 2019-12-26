package me.nekomatamune.ygomaker.fx

import javafx.collections.FXCollections
import javafx.collections.FXCollections.observableList
import javafx.fxml.FXML
import javafx.scene.control.ComboBox
import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import javafx.scene.control.TextField
import me.nekomatamune.ygomaker.*
import mu.KotlinLogging

private val logger = KotlinLogging.logger { }

typealias CardSelectedHandler = (Card) -> Unit

class CardListController {

	private var pack: Pack = Pack()

	@FXML private lateinit var packNameTextField: TextField
	@FXML private lateinit var packCodeTextField: TextField
	@FXML private lateinit var languageComboBox: ComboBox<Language>
	@FXML private lateinit var cardListView: ListView<Card>

	lateinit var cardSelectedHandler: CardSelectedHandler

	private var disableOnSelectCard = false


	@FXML
	private fun initialize() {
		logger.debug { "Initializing CardList" }

		sequenceOf(
			packNameTextField, packCodeTextField, languageComboBox
		).forEach {
			it.addSimpleListener { onModifyPackInfo() }
		}

		cardListView.addSimpleListener { onSelectCard() }
		cardListView.setCellFactory { CardListCell() }

		languageComboBox.items = observableList(Language.values().toList())
		languageComboBox.selectionModel.selectFirst()

	}

	private fun onModifyPackInfo() {
		logger.trace { "Pack info updated" }
		pack = pack.copy(
			name = packNameTextField.text,
			code = packCodeTextField.text,
			language = languageComboBox.selectionModel.selectedItem
		)
	}

	private fun onSelectCard() {
		if (disableOnSelectCard) {
			return
		}

		cardListView.selectionModel.selectedItem?.let {
			cardSelectedHandler(it)
		}
	}

	fun onModifyCard(card: Card?): Result<Unit> {
		if (cardListView.selectionModel.selectedItem == null) {
			return Result.success()
		}

		disableOnSelectCard = true

		val mergedCard = card?.let {
			cardListView.selectionModel.selectedItem.copy(
				name = it.name,
				type = it.type,
				monster = it.monster,
				code = it.code,
				effect = it.effect,
				image = it.image
			)
		}

		val cards = cardListView.items
		val selectIdx = cardListView.selectionModel.selectedIndex
		cards[selectIdx] = mergedCard
		pack = pack.copy(cards = cards)

		disableOnSelectCard = false
		return Result.success()
	}

	fun setPack(pack: Pack) {
		logger.info { "Pack ${pack.name} (${pack.code})" }

		packNameTextField.text = pack.name
		packCodeTextField.text = pack.code
		languageComboBox.selectionModel.select(pack.language)

		cardListView.apply {
			items = FXCollections.observableArrayList(pack.cards)
			selectionModel.selectFirst()
		}

		this.pack = pack
	}

	fun getPack() = pack


	fun newCard() {
		val newCard = Card()
		pack = pack.copy(cards = pack.cards + newCard)

		cardListView.items.add(newCard)
		cardListView.selectionModel.selectFirst()
	}
}

/**
 * Dictates how a [Card] should be shown as a list item.
 */
private class CardListCell : ListCell<Card>() {
	override fun updateItem(item: Card?, empty: Boolean) {
		super.updateItem(item, empty)
		if (!empty) {
			text = item?.toShortString()
		}
	}
}
