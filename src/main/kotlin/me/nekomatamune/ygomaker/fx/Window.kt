package me.nekomatamune.ygomaker.fx

import javafx.application.Platform
import javafx.fxml.FXML
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import javafx.scene.control.MenuItem
import javafx.stage.DirectoryChooser
import me.nekomatamune.ygomaker.Card
import me.nekomatamune.ygomaker.Command
import me.nekomatamune.ygomaker.Pack
import me.nekomatamune.ygomaker.deepCopyTo
import me.nekomatamune.ygomaker.readFrom
import me.nekomatamune.ygomaker.success
import mu.KotlinLogging
import java.nio.file.Files
import java.nio.file.Path

private val logger = KotlinLogging.logger { }

class Window {

	// region states
	private var packDir = Command.dataDir.resolve(Command.packCode)
	private lateinit var card: Card
	// endregion

	// region subview controllers
	@FXML private lateinit var loadPackMenuItem: MenuItem
	@FXML private lateinit var savePackMenuItem: MenuItem
	@FXML private lateinit var savePackAsMenuItem: MenuItem
	@FXML private lateinit var newCardMenuItem: MenuItem
	@FXML private lateinit var renderMenuItem: MenuItem
	@FXML private lateinit var exitMenuItem: MenuItem

	@FXML lateinit var cardListController: CardListCtrl
	@FXML lateinit var cardRendererController: CardRendererController
	@FXML lateinit var cardFormController: CardFormCtrl
	// endregion

	@FXML
	fun initialize() {
		logger.info { "Initializing Window..." }

		loadPackMenuItem.setOnAction {
			cardListController.loadPack().alertFailure()
		}
		savePackMenuItem.setOnAction { cardListController.savePack().alertFailure() }
		savePackAsMenuItem.setOnAction { cardListController.savePackAs().alertFailure() }
		newCardMenuItem.setOnAction { cardListController.addCard().alertFailure() }
		renderMenuItem.setOnAction { cardRendererController.render(card, packDir) }
		exitMenuItem.setOnAction { onExitMenuItem() }


		cardListController.cardSelectedHandler = { card, packDir ->
			cardFormController.setState(card, packDir)
			cardRendererController.render(card, packDir)
		}

		cardFormController.cardModifiedHandler = {
			card = it
			success()
		}

		logger.info { "Setup completed!" }
		cardListController.loadPack(packDir)
	}

	private fun loadPack(packDir: Path? = null) {
		val packDir = packDir ?: DirectoryChooser().apply {
			title = "Select a pack directory"
			initialDirectory = Command.dataDir.toFile()
		}.showDialog(null).toPath()

		val pack = Pack.readFrom(packDir)
		cardListController.updatePack(pack)
		this.packDir = packDir
	}

	private fun onExitMenuItem() {
		logger.debug { "onExitMenuItem()" }

		Alert(Alert.AlertType.CONFIRMATION).apply {
			headerText = "Exiting YGOMaker..."
			contentText = "All unsaved changes will be discard. Proceed?"
		}.showAndWait().filter(ButtonType.OK::equals).ifPresent {
			logger.info { "Exiting app..." }
			Platform.exit()
		}
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

				//cardListController.getPack().writeTo(packDir)
			}
		}
	}


	private fun newCard() {
		//		cardListController.getPack().let {
		//			val newPack = it.copy(cards = it.cards + Card())
		//			cardListController.setPack(newPack, selectLast = true)
		//		}
	}

}