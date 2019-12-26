package me.nekomatamune.ygomaker.fx

import javafx.fxml.FXML
import javafx.stage.DirectoryChooser
import me.nekomatamune.ygomaker.Command
import me.nekomatamune.ygomaker.Event
import me.nekomatamune.ygomaker.EventName
import me.nekomatamune.ygomaker.dispatcher
import mu.KotlinLogging

private val logger = KotlinLogging.logger { }

class Window {
	@FXML lateinit var menuBarController: MenuBar
	@FXML lateinit var cardListController: CardListController
	@FXML lateinit var cardRendererController: CardRendererController
	@FXML lateinit var cardFormController: CardFormController

	@FXML
	fun initialize() {
		logger.info { "Init Window..." }

		menuBarController.menuActionHandler = {
			when (it) {
				MenuAction.LOAD_PACK -> loadPack()
				MenuAction.SAVE_PACK -> savePack()
			}
		}


		dispatcher.register(EventName.SELECT_CARD) {
			cardFormController.setCard(it.card!!, it.packDir!!)
		}
		dispatcher.register(EventName.SAVE_PACK_AS) {
			cardListController.saveAsPack(it.packDir!!)
		}


		cardFormController.cardModifiedHandler = {
			cardListController.onModifyCard(it)
			cardRendererController.setCard(it)
		}

		dispatcher.register(EventName.NEW_CARD) { cardListController.newCard() }

		dispatcher.register(EventName.SELECT_CARD) {
			cardRendererController.setCard(it.card!!)
			cardRendererController.render()
		}

		dispatcher.register(EventName.RENDER) {
			cardRendererController.render()
		}

		cardListController.loadPack(Command.dataDir.resolve(Command.packCode))
	}

	private fun loadPack() {
		val packDir = DirectoryChooser().apply {
			title = "Select a pack directory"
			initialDirectory = Command.dataDir.toFile()
		}.showDialog(null).toPath()

		cardListController.loadPack(packDir)
	}

	private fun savePack() {
		cardListController.savePack()
	}
}