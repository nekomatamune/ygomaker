package me.nekomatamune.ygomaker.fx

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
				cardListView.items.setAll(value.cards)
				cardListView.selectionModel.selectFirst()
			}
		}

	private var selectedCard: Card
		get() = cardListView.selectionModel.selectedItem
		set(value) {
			handlerLock.lockAndRun {
				cardListView.items[cardListView.selectionModel.selectedIndex] = value
			}
		}

	// endregion

	var cardSelectedHandler: (Card) -> Result<Unit> = {
		failure(IllegalStateException("Handler not set"))
	}

	private val handlerLock = HandlerLock()


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
		cardListView.apply {
			setCellFactory { CardListCell() }
			selectionModel.selectedItemProperty()
					.addListener(handlerLock) { _, newValue ->
						cardSelectedHandler(newValue).alertFailure()
					}
		}


		//
		//		languageComboBox.items = observableList(Language.values().toList())
		//		languageComboBox.selectionModel.selectFirst()

	}

	fun updatePack(newPack: Pack) { pack = newPack}
	fun modifySelectedCard(newCard: Card) { selectedCard = newCard}

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
