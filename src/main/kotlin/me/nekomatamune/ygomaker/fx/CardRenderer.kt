package me.nekomatamune.ygomaker.fx

import javafx.fxml.FXML
import javafx.scene.canvas.Canvas
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.BorderPane
import javafx.scene.paint.Color
import me.nekomatamune.ygomaker.Event
import me.nekomatamune.ygomaker.EventType
import me.nekomatamune.ygomaker.dispatcher
import me.nekomatamune.ygomaker.success
import mu.KotlinLogging


private val logger = KotlinLogging.logger {  }

class CardRenderer {

	@FXML private lateinit var rootPane: BorderPane

	@FXML
	fun initialize() {
		logger.debug { "Initializing CardRenderer" }

		dispatcher.register(EventType.MODIFY_CARD) {
			onModifyCard(it)
		}
		dispatcher.register(EventType.SELECT_CARD) {
			onModifyCard(it)
		}
	}

	private fun onModifyCard(event: Event): Result<Unit> {

		val canvas = Canvas(200.0, 400.0)
		val gc = canvas.graphicsContext2D

		gc.stroke = Color.RED
		gc.strokeRect(10.0, 10.0, 20.0, 20.0)


		rootPane.center = canvas

		return Result.success()
	}
}