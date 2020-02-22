package me.nekomatamune.ygomaker.fx

import javafx.collections.FXCollections
import javafx.collections.FXCollections.observableList
import javafx.fxml.FXML
import javafx.scene.control.ComboBox
import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import javafx.scene.control.TextField
import me.nekomatamune.ygomaker.Card
import me.nekomatamune.ygomaker.Language
import me.nekomatamune.ygomaker.Pack
import me.nekomatamune.ygomaker.Result
import me.nekomatamune.ygomaker.failure
import me.nekomatamune.ygomaker.success
import me.nekomatamune.ygomaker.toShortString
import mu.KotlinLogging
import java.util.logging.Handler

private val logger = KotlinLogging.logger { }

class CardListCtrl {

	// region FXML components and backed properties
	@FXML private lateinit var packNameTextField: TextField
	@FXML private lateinit var packCodeTextField: TextField
	@FXML private lateinit var languageComboBox: ComboBox<Language>
	@FXML private lateinit var cardListView: ListView<Card>

	private var pack: Pack
		get() = Pack(
				name = packNameTextField.text,
				code = packCodeTextField.text,
				language = languageComboBox.selectionModel.selectedItem,
				cards = cardListView.items.toList()
		)
		set(value) {
			handlerLock.lockAndRun {
				packNameTextField.text = value.name
				packCodeTextField.text = value.code
				languageComboBox.selectionModel.select(value.language)
				cardListView.items = observableList(value.cards)
			}
		}
	// endregion

	private val handlerLock = HandlerLock()

	var cardSelectedHandler: (Card) -> Result<Unit> = {
		failure(IllegalStateException("Handler not set"))
	}

	private var disableOnSelectCard = false


	@FXML
	private fun initialize() {
		logger.debug { "Initializing CardList" }

//		sequenceOf(
//				packNameTextField, packCodeTextField, languageComboBox
//		).forEach {
//			it.addSimpleListener { onModifyPackInfo() }
//		}
//
//		cardListView.addSimpleListener { onSelectCard() }
		cardListView.setCellFactory { CardListCell() }
//
//		languageComboBox.items = observableList(Language.values().toList())
//		languageComboBox.selectionModel.selectFirst()

	}

	fun setState(newPack: Pack) {
		pack = newPack
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
			return success()
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
		return success()
	}

	fun setPack(pack: Pack, selectLast: Boolean = false) {
		logger.info { "Pack ${pack.name} (${pack.code})" }

		packNameTextField.text = pack.name
		packCodeTextField.text = pack.code
		languageComboBox.selectionModel.select(pack.language)

		cardListView.apply {
			items = FXCollections.observableArrayList(pack.cards)

			if (selectLast)
				selectionModel.selectLast()
			else
				selectionModel.selectFirst()
		}

		this.pack = pack
	}

}

/**
 * Dictates how a [Card] should be shown as a list item.
 */
private class CardListCell : ListCell<Card>() {
	override fun updateItem(item: Card?, empty: Boolean) {
		println("update item")
		super.updateItem(item, empty)
		if (!empty) {
			text = item?.toShortString()
		}
	}
}
