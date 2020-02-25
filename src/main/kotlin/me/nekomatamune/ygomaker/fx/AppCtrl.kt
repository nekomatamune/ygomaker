package me.nekomatamune.ygomaker.fx

import javafx.application.Platform
import javafx.fxml.FXML
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import javafx.scene.control.MenuItem
import me.nekomatamune.ygomaker.Card
import me.nekomatamune.ygomaker.Command
import me.nekomatamune.ygomaker.success
import mu.KotlinLogging

private val logger = KotlinLogging.logger { }

class AppCtrl(
		cmd: Command = Command
) {

	// region states
	private var packDir = cmd.dataDir.resolve(cmd.packCode)
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
}