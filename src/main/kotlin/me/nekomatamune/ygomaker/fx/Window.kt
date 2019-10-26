package me.nekomatamune.ygomaker.fx

import javafx.fxml.FXML
import me.nekomatamune.ygomaker.Command
import me.nekomatamune.ygomaker.Event
import me.nekomatamune.ygomaker.EventName
import me.nekomatamune.ygomaker.dispatchEvent
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