package me.nekomatamune.ygomaker.fx

import javafx.application.Platform
import javafx.fxml.FXML
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import javafx.scene.control.MenuItem
import javafx.stage.DirectoryChooser
import me.nekomatamune.ygomaker.Command
import me.nekomatamune.ygomaker.Event
import me.nekomatamune.ygomaker.EventName
import me.nekomatamune.ygomaker.dispatchEvent
import mu.KotlinLogging
import java.nio.file.Files

private val logger = KotlinLogging.logger { }

class MenuBar {

	@FXML private lateinit var loadPackMenuItem: MenuItem
	@FXML private lateinit var savePackMenuItem: MenuItem
	@FXML private lateinit var savePackAsMenuItem: MenuItem
	@FXML private lateinit var exitMenuItem: MenuItem

	@FXML
	private fun initialize() {
		logger.debug { "Initializing MenuBar" }
		loadPackMenuItem.onAction = ::onLoadPackMenuItem.asEventHandler()
		savePackMenuItem.onAction = ::onSavePackMenuItem.asEventHandler()
		savePackAsMenuItem.onAction = ::onSavePackAsMenuItem.asEventHandler()
		exitMenuItem.onAction = ::onExitMenuItem.asEventHandler()
	}

	private fun onLoadPackMenuItem() {
		logger.debug { "onLoadPackMenuItem()" }

		val packDir = DirectoryChooser().apply {
			title = "Select a pack directory"
			initialDirectory = Command.dataDir.toFile()
		}.showDialog(null).toPath()

		dispatchEvent(Event(
			name = EventName.LOAD_PACK, packDir = packDir))
	}

	private fun onSavePackMenuItem() {
		logger.debug { "onSavePackMenuItem()" }
		dispatchEvent(Event(name = EventName.SAVE_PACK))
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
				dispatchEvent(
					Event(
						name = EventName.SAVE_PACK_AS,
						packDir = newPackDir))
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