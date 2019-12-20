package me.nekomatamune.ygomaker.fx

import javafx.fxml.FXML
import me.nekomatamune.ygomaker.EventName
import me.nekomatamune.ygomaker.dispatcher
import me.nekomatamune.ygomaker.success
import mu.KotlinLogging

private val logger = KotlinLogging.logger { }

class Window {
	@FXML lateinit var cardListController: CardListController
	@FXML lateinit var cardRendererController: CardRendererController
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


		dispatcher.register(
			EventName.MODIFY_CARD) { cardListController.onModifyCard(it.card) }


		dispatcher.register(EventName.NEW_CARD) { cardListController.newCard() }

		dispatcher.register(EventName.SELECT_CARD) {
			cardRendererController.setCard(it.card!!)
			cardRendererController.render()
		}

		dispatcher.register(EventName.MODIFY_CARD) {
			cardRendererController.setCard(it.card!!)
			Result.success()
		}

		dispatcher.register(EventName.RENDER) {
			cardRendererController.render()
		}
	}
}