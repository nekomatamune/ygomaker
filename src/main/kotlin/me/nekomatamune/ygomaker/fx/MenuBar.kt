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
import me.nekomatamune.ygomaker.dispatcher
import mu.KotlinLogging
import java.nio.file.Files

private val logger = KotlinLogging.logger { }

enum class MenuAction {
	LOAD_PACK, SAVE_PACK, SAVE_PACK_AS, NEW_CARD
}
typealias MenuActionHandler = (MenuAction) -> Unit

class MenuBar {

	@FXML private lateinit var loadPackMenuItem: MenuItem
	@FXML private lateinit var savePackMenuItem: MenuItem
	@FXML private lateinit var savePackAsMenuItem: MenuItem
	@FXML private lateinit var newCardMenuItem: MenuItem
	@FXML private lateinit var renderMenuItem: MenuItem
	@FXML private lateinit var exitMenuItem: MenuItem

	lateinit var menuActionHandler: MenuActionHandler

	@FXML
	private fun initialize() {
		logger.debug { "Initializing MenuBar" }
		loadPackMenuItem.setOnAction { menuActionHandler(MenuAction.LOAD_PACK) }
		savePackMenuItem.setOnAction { menuActionHandler(MenuAction.SAVE_PACK) }
		savePackAsMenuItem.setOnAction { menuActionHandler(MenuAction.SAVE_PACK_AS) }
		newCardMenuItem.setOnAction { menuActionHandler(MenuAction.NEW_CARD) }
		renderMenuItem.setOnAction {
			dispatcher.dispatch(Event(EventName.RENDER))
		}
		exitMenuItem.setOnAction { onExitMenuItem() }
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