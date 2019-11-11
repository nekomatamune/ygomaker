package me.nekomatamune.ygomaker.fx

import javafx.fxml.FXML
import javafx.scene.canvas.Canvas
import javafx.scene.layout.BorderPane
import me.nekomatamune.ygomaker.*
import mu.KotlinLogging


private val logger = KotlinLogging.logger { }

class CardRenderer {

	@FXML private lateinit var rootPane: BorderPane

	private lateinit var card: Card

	@FXML
	fun initialize() {
		logger.debug { "Initializing CardRenderer" }

		dispatcher.register(EventType.SELECT_CARD) {
			card = it.card!!
			render()
		}

		dispatcher.register(EventType.MODIFY_CARD) {
			card = it.card!!.copy(image = card.image)
			render()
		}

		dispatcher.register(EventType.MODIFY_CARD_IMAGE) {
			card = card.copy(image = it.image!!)
			render()
		}

	}

	private fun render(): Result<Unit> {
		logger.info { "Render card" }

		val canvas = Canvas(400.0, 570.0)
		val gc = canvas.graphicsContext2D

		val cardFrameImage = getCardFrame(card).onFailure {
			return Result.failure(it)
		}

		gc.drawImage(cardFrameImage.getOrNull()!!, 0.0, 0.0, 400.0, 570.0)

		rootPane.center = canvas

		return Result.success()

	}
}