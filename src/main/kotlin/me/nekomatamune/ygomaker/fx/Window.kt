package me.nekomatamune.ygomaker.fx

import javafx.fxml.FXML
import me.nekomatamune.ygomaker.EventName
import me.nekomatamune.ygomaker.dispatcher
import mu.KotlinLogging

private val logger = KotlinLogging.logger { }

class Window {
	@FXML lateinit var cardListController: CardListController
	@FXML lateinit var cardFormController: CardFormController

	@FXML
	fun initialize() {
		logger.info { "Init Window..." }

		dispatcher.register(EventName.SELECT_CARD) {
			cardFormController.setCard(it.card!!, it.packDir!!)
		}
		dispatcher.register(EventName.LOAD_PACK) {
			cardListController.loadPack(it.packDir!!)
		}
		dispatcher.register(EventName.SAVE_PACK) { cardListController.savePack() }
		dispatcher.register(EventName.SAVE_PACK_AS) {
			cardListController.saveAsPack(it.packDir!!)
		}

	}
}