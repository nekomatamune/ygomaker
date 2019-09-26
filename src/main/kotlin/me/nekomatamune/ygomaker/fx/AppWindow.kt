package me.nekomatamune.ygomaker.fx

import me.nekomatamune.ygomaker.Command
import javafx.fxml.FXML
import mu.KotlinLogging

private val logger = KotlinLogging.logger { }

class AppWindow {
	@FXML
	lateinit var menuBarController: MenuBar

	@FXML
	lateinit var cardListController: CardList

	@FXML
	lateinit var cardFormController: CardForm

	@FXML
	fun initialize() {
		logger.info { "Initializing AppWindow" }

		dispatchEvent(Event(
			name = EventName.LOAD_PACK,
			packDir = Command.dataDir.resolve(Command.packCode)
		))
	}

}