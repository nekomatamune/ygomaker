package me.nekomatamune.ygomaker.fx

import com.google.common.io.Resources
import javafx.fxml.FXML
import javafx.scene.canvas.Canvas
import javafx.scene.image.Image
import javafx.scene.layout.BorderPane
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.Text
import javafx.scene.text.TextAlignment
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

		dispatcher.register(EventName.SELECT_CARD) {
			card = it.card!!
			render()
		}

		dispatcher.register(EventName.MODIFY_CARD) {
			card = it.card!!.copy(image = card.image)
			render()
		}

		dispatcher.register(EventName.MODIFY_CARD_IMAGE) {
			card = card.copy(image = it.image!!)
			Result.success()
		}

		dispatcher.register(EventName.RENDER) {
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

		logger.info { "supposed font is ${p.nameFont}" }

		gc.font = Font(p.nameFont.name, p.nameFont.size)
		gc.fill = getCardNameColor(card)
		gc.textAlign = TextAlignment.LEFT
		gc.fillText(card.name, p.nameRect.x, p.nameRect.y + p.nameRect.h,
			p.nameRect.w)

		logger.info { "font is ${gc.font.toString()}" }


		getAttribute(card).onFailure {
			return Result.failure(it)

		}.onSuccess { attributeImage ->
			gc.drawImage(attributeImage, p.attributeRect.x, p.attributeRect.y,
				p.attributeRect.w, p.attributeRect.h)
		}

		getSymbol(card).onFailure {
			return Result.failure(it)
		}.onSuccess { symbolImage ->
			when (card.type) {
				CardType.XYZ_MONSTER -> {
					for (i in 0 until card.monster!!.level) {
						gc.drawImage(symbolImage!!,
							p.rankRect.x + i * (p.rankRect.w + p.rankSpacing),
							p.rankRect.y,
							p.rankRect.w, p.rankRect.h)
					}
				}
				in MONSTER_CARD_TYPES -> {
					for (i in 0 until card.monster!!.level) {
						gc.drawImage(symbolImage!!,
							p.levelRect.x - i * (p.levelRect.w + p.levelSpacing),
							p.levelRect.y,
							p.levelRect.w, p.levelRect.h)
					}
				}

				else -> {
					getSpellTrapText(card)?.let { spellTrapText ->
						gc.fill = Color.BLACK
						gc.font = Font(p.spellTrapTypeFont.name, p.spellTrapTypeFont.size)
						gc.textAlign = TextAlignment.RIGHT
						gc.fillText(spellTrapText, p.spellTrapTypeRect.x,
							p.spellTrapTypeRect.y + p.spellTrapTypeRect.h)
					}

					symbolImage?.let {
						gc.drawImage(it, p.spellTrapTypeSymbolRect.x,
							p.spellTrapTypeSymbolRect.y, p.spellTrapTypeSymbolRect.w,
							p.spellTrapTypeSymbolRect.h)
					}
				}
			}
		}

		card.image?.let {
			val imagePath = Command.dataDir.resolve(Command.packCode).resolve(it.file)
			logger.info { "Image: $imagePath" }
			val image = Image(imagePath.toUri().toString())
			logger.info {"image: $image" }


			gc.drawImage(image, it.x.toDouble(), it.y.toDouble(), it.size.toDouble(),
				it.size.toDouble(),
				p.imageRect.x, p.imageRect.y, p.imageRect.w,
				p.imageRect.h)
		}


		rootPane.center = canvas

		canvas.setOnMouseMoved { infoText.text = "(${it.x}, ${it.y})" }

		return Result.success()
	}

}