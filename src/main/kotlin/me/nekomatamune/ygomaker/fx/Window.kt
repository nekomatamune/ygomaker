package me.nekomatamune.ygomaker.fx

import javafx.fxml.FXML
import me.nekomatamune.ygomaker.EventName
import me.nekomatamune.ygomaker.dispatcher
import mu.KotlinLogging

private val logger = KotlinLogging.logger { }

class Window {
	@FXML lateinit var cardFormController: CardFormController

	@FXML
	fun initialize() {
		logger.info { "Init Window..." }

		dispatcher.register(EventName.SELECT_CARD) {
			cardFormController.setCard(it.card!!)
		}
	}
}