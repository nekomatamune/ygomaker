package me.nekomatamune.ygomaker.fx

import javafx.fxml.FXML
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import javafx.stage.DirectoryChooser
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import me.nekomatamune.ygomaker.Command
import me.nekomatamune.ygomaker.Pack
import mu.KotlinLogging
import java.io.FileNotFoundException
import java.nio.file.Files
import java.nio.file.Path

private val logger = KotlinLogging.logger { }
private val json = Json(JsonConfiguration.Stable.copy(prettyPrint = true))

class Window {
	@FXML lateinit var menuBarController: MenuBar
	@FXML lateinit var cardListController: CardListController
	@FXML lateinit var cardRendererController: CardRendererController
	@FXML lateinit var cardFormController: CardFormController

	@FXML
	fun initialize() {
		logger.info { "Init Window..." }

		menuBarController.menuActionHandler = {
			when (it) {
				MenuAction.LOAD_PACK -> loadPack()
				MenuAction.SAVE_PACK -> savePack()
				MenuAction.SAVE_PACK_AS -> savePackAs()
				MenuAction.NEW_CARD -> cardListController.newCard()
				MenuAction.RENDER_CARD -> cardRendererController.render()
			}
		}

		cardListController.cardSelectedHandler = { card, packDir ->
			cardFormController.setCard(card, packDir)
			cardRendererController.setCard(card)
			cardRendererController.render()
		}

		cardFormController.cardModifiedHandler = {
			cardListController.onModifyCard(it)
			cardRendererController.setCard(it)
		}

		loadPack(Command.dataDir.resolve(Command.packCode))
	}

	private fun loadPack(packDir: Path? = null) {

		val packDir = packDir ?: DirectoryChooser().apply {
			title = "Select a pack directory"
			initialDirectory = Command.dataDir.toFile()
		}.showDialog(null).toPath()

		cardListController.packDir = packDir

		val cardFile = packDir.resolve("pack.json")
		if (!cardFile.toFile().isFile) {
			throw FileNotFoundException(cardFile.toString())
		}

		val pack = json.parse(Pack.serializer(), cardFile.toFile().readText())
		cardListController.loadPack(pack)
	}

	private fun savePack() {
		cardListController.savePack()
	}

	private fun savePackAs() {
		logger.debug { "onSavePackAsMenuItem()" }

		val newPackDir = DirectoryChooser().apply {
			title = "Enter or select a new pack directory"
			initialDirectory = Command.dataDir.toFile()
		}.showDialog(null).toPath()

		if (Files.exists(newPackDir)) {
			Alert(Alert.AlertType.CONFIRMATION).apply {
				headerText = "Overwriting existing pack..."
				contentText = "This will overwrite the existing pack ${newPackDir.fileName}. Proceed?"
			}.showAndWait().filter(ButtonType.OK::equals).ifPresent {
				logger.info { "Writing pack to ${newPackDir.fileName}" }
				cardListController.saveAsPack(newPackDir)
			}
		}
	}
}