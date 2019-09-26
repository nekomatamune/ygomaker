package me.nekomatamune.ygomaker.fx

import me.nekomatamune.ygomaker.Command
import javafx.application.Platform
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import javafx.scene.control.MenuItem
import javafx.stage.DirectoryChooser
import mu.KotlinLogging

private val logger = KotlinLogging.logger { }

class MenuBar {

	@FXML
	private lateinit var loadPackMenuItem: MenuItem

	@FXML
	private lateinit var exitMenuItem: MenuItem

	@FXML
	private fun initialize() {
		logger.debug { "Initializing MenuBar" }

		loadPackMenuItem.onAction = EventHandler<ActionEvent> {
			onLoadPackMenuItem()
		}

		exitMenuItem.onAction = EventHandler<ActionEvent> {
			onExitMenuItem()
		}
	}

	private fun onLoadPackMenuItem() {
		logger.debug { "onLoadPackMenuItem()" }

		val packDir = DirectoryChooser().apply {
			title = "Select a pack directory"
			initialDirectory = Command.dataDir.toFile()
		}.showDialog(null).toPath()

		dispatchEvent(Event(name = EventName.LOAD_PACK, packDir = packDir))
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