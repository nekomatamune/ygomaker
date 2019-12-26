package me.nekomatamune.ygomaker.fx

import javafx.fxml.FXML
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import javafx.stage.DirectoryChooser
import me.nekomatamune.ygomaker.Command
import mu.KotlinLogging
import java.nio.file.Files

private val logger = KotlinLogging.logger { }

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

		cardListController.loadPack(Command.dataDir.resolve(Command.packCode))
	}

	private fun loadPack() {
		val packDir = DirectoryChooser().apply {
			title = "Select a pack directory"
			initialDirectory = Command.dataDir.toFile()
		}.showDialog(null).toPath()

		cardListController.loadPack(packDir)
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