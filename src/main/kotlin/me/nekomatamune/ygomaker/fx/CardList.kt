package me.nekomatamune.ygomaker.fx

import javafx.collections.FXCollections
import javafx.fxml.FXML
import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import javafx.scene.control.TextField
import javafx.scene.text.Text
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import me.nekomatamune.ygomaker.Card
import me.nekomatamune.ygomaker.Pack
import me.nekomatamune.ygomaker.toShortString
import mu.KotlinLogging
import java.io.FileNotFoundException
import java.nio.file.Path

private val logger = KotlinLogging.logger { }

class CardList {

	private lateinit var pack: Pack
	private lateinit var packDir: Path

	@FXML
	private lateinit var packDirText: Text

	@FXML private lateinit var packNameTextField: TextField
	@FXML private lateinit var packCodeTextField: TextField

	@FXML
	private lateinit var cardListView: ListView<Card>

	private var disableOnSelectCard = false

	@FXML
	private fun initialize() {
		logger.debug { "Initializing CardList" }

		cardListView.apply {
			setCellFactory { CardListCell() }
			selectionModel.selectedItemProperty().addListener { _, _, newValue ->
				onSelectCard(newValue)
			}
		}

		registerEventHandler(EventName.LOAD_PACK) {
			logger.debug { "Handling LOAD_PACK event" }
			loadPack(it.packDir!!)
		}

		registerEventHandler(EventName.MODIFY_CARD) {
			onModifyCard(it)
		}

		registerEventHandler(EventName.MODIFY_CARD_IMAGE) {
			onModifyCardImage(it)
		}
	}

	private fun onSelectCard(newValue: Card) {
		if (disableOnSelectCard) {
			return
		}

		logger.info("Selected card ${newValue.toShortString()}")
		dispatchEvent(Event(
			name = EventName.SELECT_CARD,
			card = newValue,
			packDir = packDir
		))
	}

	private fun onModifyCard(event: Event): Result<Unit> {

		disableOnSelectCard = true

		val mergedCard = event.card?.let {
			cardListView.selectionModel.selectedItem.copy(
				name = it.name,
				type = it.type,
				monster = it.monster,
				code = it.code,
				effect = it.effect
			)
		}

		logger.debug { "Merged card: $mergedCard" }
		logger.debug { "selected idx: ${cardListView.selectionModel.selectedIndex}" }
		logger.debug { "selected size: ${cardListView.items.size}" }

		val cards = cardListView.items
		val selectIdx = cardListView.selectionModel.selectedIndex
		cards[selectIdx] = mergedCard
		//cardListView.items[cardListView.selectionModel.selectedIndex] = mergedCard

		disableOnSelectCard = false
		return Result.success(Unit)
	}

	private fun onModifyCardImage(event: Event): Result<Unit> {
		disableOnSelectCard = true

		val mergedCard = event.image!!.let {
			cardListView.selectionModel.selectedItem.copy(
				image = it
			)
		}

		val cards = cardListView.items
		val selectIdx = cardListView.selectionModel.selectedIndex
		cards[selectIdx] = mergedCard

		disableOnSelectCard = false
		return Result.success(Unit)
	}


	private fun loadPack(packDir: Path): Result<Unit> {
		logger.debug { "Loading pack from: $packDir" }

		val cardFile = packDir.resolve("pack.json")
		if (!cardFile.toFile().isFile) {
			return Result.failure(FileNotFoundException(cardFile.toString()))
		}

		pack = Json(JsonConfiguration.Stable).parse(
			Pack.serializer(), cardFile.toFile().readText())

		logger.info { "Pack ${pack.name} (${pack.code})" }

		packDirText.text = cardFile.toString()
		packNameTextField.text = pack.name
		packCodeTextField.text = pack.code
		this.packDir = packDir
		
		cardListView.apply {
			items = FXCollections.observableArrayList(pack.cards)
			selectionModel.selectFirst()
		}

		return Result.success(Unit)
	}

	fun updateSelectedCard(card: Card) {
		// TODO fill in card code
		cardListView.apply {
			items[selectionModel.selectedIndex] = card
		}
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
