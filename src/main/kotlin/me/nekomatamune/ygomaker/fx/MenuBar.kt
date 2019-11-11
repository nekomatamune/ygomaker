package me.nekomatamune.ygomaker.fx

import javafx.application.Platform
import javafx.fxml.FXML
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import javafx.scene.control.MenuItem
import javafx.stage.DirectoryChooser
import me.nekomatamune.ygomaker.Command
import me.nekomatamune.ygomaker.Event
import me.nekomatamune.ygomaker.EventType
import me.nekomatamune.ygomaker.dispatcher
import mu.KotlinLogging
import java.nio.file.Files

private val logger = KotlinLogging.logger { }

class MenuBar {

	@FXML private lateinit var loadPackMenuItem: MenuItem
	@FXML private lateinit var savePackMenuItem: MenuItem
	@FXML private lateinit var savePackAsMenuItem: MenuItem
	@FXML private lateinit var renderMenuItem: MenuItem
	@FXML private lateinit var exitMenuItem: MenuItem

	@FXML
	private fun initialize() {
		logger.debug { "Initializing MenuBar" }
		loadPackMenuItem.setOnAction { onLoadPackMenuItem() }
		loadPackMenuItem.setOnAction { onLoadPackMenuItem() }
		savePackMenuItem.setOnAction { onSavePackMenuItem() }
		savePackAsMenuItem.setOnAction { onSavePackAsMenuItem() }
		renderMenuItem.setOnAction {
			dispatcher.dispatch(Event(EventType.RENDER))
		}
		exitMenuItem.setOnAction { onExitMenuItem() }
	}

	private fun onLoadPackMenuItem() {
		logger.debug { "onLoadPackMenuItem()" }

		val packDir = DirectoryChooser().apply {
			title = "Select a pack directory"
			initialDirectory = Command.dataDir.toFile()
		}.showDialog(null).toPath()

		dispatcher.dispatch(Event(EventType.LOAD_PACK, packDir = packDir))
	}

	private fun onSavePackMenuItem() {
		logger.debug { "onSavePackMenuItem()" }
		dispatcher.dispatch(Event(EventType.SAVE_PACK))
	}

	private fun onSavePackAsMenuItem() {
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
				dispatcher.dispatch(Event(EventType.SAVE_PACK_AS, packDir = newPackDir))
			}
		}
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