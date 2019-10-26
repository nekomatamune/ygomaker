package me.nekomatamune.ygomaker.fx

import javafx.fxml.FXML
import me.nekomatamune.ygomaker.Command
import mu.KotlinLogging

private val logger = KotlinLogging.logger { }

class Window {

	@FXML
	fun initialize() {
		logger.info { "Initializing AppWindow" }

		dispatchEvent(Event(
			name = EventName.LOAD_PACK,
			packDir = Command.dataDir.resolve(Command.packCode)
		))
	}

}