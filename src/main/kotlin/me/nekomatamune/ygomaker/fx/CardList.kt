package me.nekomatamune.ygomaker.fx

import javafx.collections.FXCollections
import javafx.collections.FXCollections.observableList
import javafx.fxml.FXML
import javafx.scene.control.ComboBox
import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import javafx.scene.control.TextField
import javafx.scene.text.Text
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import me.nekomatamune.ygomaker.*
import mu.KotlinLogging
import java.io.FileNotFoundException
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes

private val logger = KotlinLogging.logger { }

class CardList {

	private var pack: Pack = Pack()
	private lateinit var packDir: Path

	@FXML private lateinit var packDirText: Text
	@FXML private lateinit var packNameTextField: TextField
	@FXML private lateinit var packCodeTextField: TextField
	@FXML private lateinit var languageComboBox: ComboBox<Language>
	@FXML private lateinit var cardListView: ListView<Card>

	private var disableOnSelectCard = false
	private val json = Json(JsonConfiguration.Stable.copy(prettyPrint = true))
	private val backupper by lazy {
		Backupper(Command.dataDir.resolve("bak"), 10)
	}

	@FXML
	private fun initialize() {
		logger.debug { "Initializing CardList" }

		sequenceOf(
			packNameTextField, packCodeTextField, languageComboBox
		).forEach {
			it.addSimpleListener { onModifyPackInfo() }
		}

		cardListView.addSimpleListener { onSelectCard() }

		languageComboBox.items = observableList(Language.values().toList())
		languageComboBox.selectionModel.selectFirst()
		cardListView.setCellFactory { CardListCell() }

		dispatcher.register(EventName.LOAD_PACK) { loadPack(it) }
		dispatcher.register(EventName.SAVE_PACK) { savePack(it) }
		dispatcher.register(EventName.SAVE_PACK_AS) { saveAsPack(it) }
		dispatcher.register(EventName.MODIFY_CARD) { onModifyCard(it) }
		dispatcher.register(EventName.MODIFY_CARD_IMAGE) { onModifyCardImage(it) }
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

		dispatcher.dispatch(Event(
			EventName.SELECT_CARD,
			card = cardListView.selectionModel.selectedItem,
			packDir = packDir
		))
	}

	private fun onModifyCard(event: Event): Result<Unit> {
		if (cardListView.selectionModel.selectedItem == null) {
			return Result.success(Unit)
		}


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

		val cards = cardListView.items
		val selectIdx = cardListView.selectionModel.selectedIndex
		cards[selectIdx] = mergedCard
		pack = pack.copy(cards = cards)

		disableOnSelectCard = false
		return Result.success(Unit)
	}

	private fun onModifyCardImage(event: Event): Result<Unit> {
		if (cardListView.selectionModel.selectedItem == null) {
			return Result.success(Unit)
		}

		disableOnSelectCard = true
		val mergedCard = event.image!!.let {
			cardListView.selectionModel.selectedItem.copy(
				image = it
			)
		}

		val cards = cardListView.items
		val selectIdx = cardListView.selectionModel.selectedIndex
		cards[selectIdx] = mergedCard
		pack = pack.copy(cards = cards)

		disableOnSelectCard = false
		return Result.success(Unit)
	}


	private fun loadPack(event: Event): Result<Unit> {
		val packDir = event.packDir!!
		logger.debug { "Loading pack from: $packDir" }

		val cardFile = packDir.resolve("pack.json")
		if (!cardFile.toFile().isFile) {
			return Result.failure(FileNotFoundException(cardFile.toString()))
		}

		pack = json.parse(Pack.serializer(), cardFile.toFile().readText())

		logger.info { "Pack ${pack.name} (${pack.code})" }

		packDirText.text = Paths.get(".").toAbsolutePath().relativize(
			cardFile.toAbsolutePath()).normalize().toString()
		packNameTextField.text = pack.name
		packCodeTextField.text = pack.code
		languageComboBox.selectionModel.select(pack.language)
		this.packDir = packDir

		cardListView.apply {
			items = FXCollections.observableArrayList(pack.cards)
			selectionModel.selectFirst()
		}

		return Result.success(Unit)
	}

	private fun savePack(event: Event): Result<Unit> {
		logger.info { "Saving pack into $packDir" }
		val cardFile = packDir.resolve("pack.json")

		backupper.backup(cardFile)

		val packJson = json.stringify(Pack.serializer(), pack)

		cardFile.toFile().writeText(packJson)

		return Result.success(Unit)
	}

	private fun saveAsPack(event: Event): Result<Unit> {
		val newPackDir = event.packDir!!

		Files.walkFileTree(packDir, object : SimpleFileVisitor<Path>() {
			override fun visitFile(file: Path,
				attrs: BasicFileAttributes): FileVisitResult {
				Files.copy(file, newPackDir.resolve(file.fileName),
					StandardCopyOption.REPLACE_EXISTING)
				return FileVisitResult.CONTINUE
			}
		})
		this.packDir = newPackDir

		savePack(Event()).let {
			if (it.isFailure) {
				return it
			}
		}

		return loadPack(event)
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
