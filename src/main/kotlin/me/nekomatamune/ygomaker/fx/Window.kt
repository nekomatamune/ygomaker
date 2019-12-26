package me.nekomatamune.ygomaker.fx

import javafx.fxml.FXML
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import javafx.stage.DirectoryChooser
import me.nekomatamune.ygomaker.*
import mu.KotlinLogging
import java.nio.file.Files
import java.nio.file.Path

private val logger = KotlinLogging.logger { }

class Window {

	private var packDir: Path = Command.dataDir.resolve(Command.packCode)

	@FXML lateinit var menuBarController: MenuBar
	@FXML lateinit var cardListController: CardListController
	@FXML lateinit var cardRendererController: CardRendererController
	@FXML lateinit var cardFormController: CardFormController

	@FXML
	fun initialize() {
		logger.info { "Initializing Window..." }

		menuBarController.menuActionHandler = {
			when (it) {
				MenuAction.LOAD_PACK -> loadPack()
				MenuAction.SAVE_PACK -> cardListController.getPack().writeTo(packDir)
				MenuAction.SAVE_PACK_AS -> savePackAs()
				MenuAction.NEW_CARD -> cardListController.newCard()
				MenuAction.RENDER_CARD -> cardRendererController.render(
					cardFormController.getCard())
			}
		}

		cardListController.cardSelectedHandler = {
			cardFormController.setCard(it, packDir)
			cardRendererController.render(it)
		}

		cardFormController.cardModifiedHandler = {
			cardListController.onModifyCard(it)
		}

		loadPack(Command.dataDir.resolve(Command.packCode))
	}

	private fun loadPack(packDir: Path? = null) {
		val packDir = packDir ?: DirectoryChooser().apply {
			title = "Select a pack directory"
			initialDirectory = Command.dataDir.toFile()
		}.showDialog(null).toPath()

		val pack = Pack.readFrom(packDir)
		cardListController.setPack(pack)
		this.packDir = packDir
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

				packDir.deepCopyTo(newPackDir)
				this.packDir = newPackDir

				cardListController.getPack().writeTo(packDir)
			}
		}
	}
}