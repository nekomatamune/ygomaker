package me.nekomatamune.ygomaker.fx

import com.google.common.io.Resources
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.scene.canvas.Canvas
import javafx.scene.layout.BorderPane
import javafx.scene.text.Text
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import me.nekomatamune.ygomaker.*
import mu.KotlinLogging


private val logger = KotlinLogging.logger { }
private val json = Json(JsonConfiguration.Stable.copy(prettyPrint = true))

class CardRenderer {

	@FXML private lateinit var rootPane: BorderPane
	@FXML private lateinit var infoText: Text

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

		dispatcher.register(EventType.RENDER) {
			render()
		}
	}

	private fun render(): Result<Unit> {
		logger.info { "Render card" }

		val paramText = Command.rendererParamFile?.toFile()?.readText()
			?: Resources.getResource("renderer_params.json").readText()

		val p = json.parse(RendererParams.serializer(), paramText)

		val canvas = Canvas(
			p.frameOrigin.x + p.frameSize.w,
			p.frameOrigin.y + p.frameSize.h
		)
		val gc = canvas.graphicsContext2D

		getCardFrame(card).onFailure {
			return Result.failure(it)

		}.onSuccess { cardFrameImage ->
			gc.drawImage(cardFrameImage, p.frameOrigin.x,
				p.frameOrigin.y,
				p.frameSize.w, p.frameSize.h)
		}

		logger.info{"supposed font is ${p.nameFont}"}

		gc.font = javafx.scene.text.Font(p.nameFont.name, p.nameFont.size)
		gc.fill = getCardNameColor(card)
		gc.fillText(card.name, p.nameRect.x, p.nameRect.y + p.nameRect.h, p.nameRect.w)

		logger.info{"font is ${gc.font.toString()}"}


		getAttribute(card).onFailure {
			return Result.failure(it)

		}.onSuccess { attributeImage ->
			gc.drawImage(attributeImage, p.attributeRect.x, p.attributeRect.y,
				p.attributeRect.w, p.attributeRect.h)
		}



		rootPane.center = canvas

		canvas.setOnMouseMoved { infoText.text = "(${it.x}, ${it.y})" }

		return Result.success()
	}

}