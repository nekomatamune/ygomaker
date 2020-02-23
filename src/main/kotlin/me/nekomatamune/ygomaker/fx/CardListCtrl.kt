package me.nekomatamune.ygomaker.fx

import javafx.fxml.FXML
import javafx.scene.control.ComboBox
import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import javafx.scene.control.TextField
import javafx.stage.FileChooser
import me.nekomatamune.ygomaker.Card
import me.nekomatamune.ygomaker.Command
import me.nekomatamune.ygomaker.FileIO
import me.nekomatamune.ygomaker.Language
import me.nekomatamune.ygomaker.Pack
import me.nekomatamune.ygomaker.Result
import me.nekomatamune.ygomaker.failure
import me.nekomatamune.ygomaker.success
import me.nekomatamune.ygomaker.toShortString
import mu.KotlinLogging
import java.nio.file.Path

private val logger = KotlinLogging.logger { }

open class CardListCtrl(
		val fileChooserFactory: () -> FileChooser = { FileChooser() },
		val fileIO: FileIO = FileIO()
) {

	// region FXML components and backed properties
	@FXML private lateinit var packNameTextField: TextField
	@FXML private lateinit var packCodeTextField: TextField
	@FXML private lateinit var languageComboBox: ComboBox<Language>
	@FXML private lateinit var cardListView: ListView<Card>

	private lateinit var packDir: Path
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

	var cardSelectedHandler: (Card, Path) -> Result<Unit> = { _, _ ->
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
						cardSelectedHandler(newValue, packDir).alertFailure()
					}
		}


		//
		//		languageComboBox.items = observableList(Language.values().toList())
		//		languageComboBox.selectionModel.selectFirst()
	}

	fun updatePackDir(newPackDir: Path) {
		packDir = newPackDir
	}

	fun updatePack(newPack: Pack) {
		pack = newPack
	}

	fun modifySelectedCard(newCard: Card) {
		selectedCard = newCard
	}

	fun loadPack(dataDir: Path = Command.dataDir): Result<Unit> {
		val fileChooser = fileChooserFactory().apply {
			title = "Select a Pack Directory"
			initialDirectory = dataDir.toFile()
		}

		val newPackDir = fileChooser.showOpenDialog(null).toPath()

		pack = fileIO.readPack(newPackDir).onFailure {
			return it
		}
		packDir = dataDir.relativize(newPackDir)

		return cardSelectedHandler(selectedCard, packDir)
	}

	fun savePack(): Result<Unit> {
		return fileIO.writePack(pack, packDir)
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