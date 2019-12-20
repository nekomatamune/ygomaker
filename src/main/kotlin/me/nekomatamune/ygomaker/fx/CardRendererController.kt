package me.nekomatamune.ygomaker.fx

import com.google.common.io.Resources
import com.sun.javafx.tk.Toolkit
import javafx.fxml.FXML
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.image.Image
import javafx.scene.layout.BorderPane
import javafx.scene.paint.Color
import javafx.scene.text.*
import javafx.scene.text.Font
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import me.nekomatamune.ygomaker.*
import mu.KotlinLogging

private val logger = KotlinLogging.logger { }
private val json = Json(JsonConfiguration.Stable.copy(prettyPrint = true))

class CardRendererController {

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

		gc.apply {
			font = p.nameFont.toFxFont()
			fill = getCardNameColor(card)
			textAlign = TextAlignment.LEFT
		}.fillText(card.name, p.nameRect)


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

						gc.apply {
							fill = Color.BLACK
							textAlign = TextAlignment.RIGHT
							font = p.spellTrapTypeFont.toFxFont()
						}.fillText(spellTrapText, p.spellTrapTypeRect)
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
			val image = Image(imagePath.toUri().toString())


			gc.drawImage(image, it.x.toDouble(), it.y.toDouble(), it.size.toDouble(),
				it.size.toDouble(),
				p.imageRect.x, p.imageRect.y, p.imageRect.w,
				p.imageRect.h)
		}


		if (card.type.isMonster()) {
			val text = getMonsterTypeText(card)

			gc.apply {
				fill = Color.BLACK
				textAlign = TextAlignment.LEFT
				font = p.monsterTypeFont.toFxFont()
			}.fillText(text, p.monsterTypeRect)

			gc.apply {
				fill = Color.BLACK
				textAlign = TextAlignment.LEFT
				font = p.atkDefFont.toFxFont()

				fillText("ATK/${card.monster!!.atk}", p.atkRect)
				fillText("DEF/${card.monster!!.def}", p.defRect)
			}
		}

		val effectFont = if (card.type.isMonster()) p.monsterEffectFont else p.spellTrapEffectFont
		val effectRect = if (card.type.isMonster()) p.monsterEffectRect else p.spellTrapEffectRect

		val multilineText = toMultilineText(card.effect,
			(effectFont.size.toInt() downTo 1).toList(),
			effectRect.w.toInt(),
			effectRect.h.toInt(),
			{
				Toolkit.getToolkit().fontLoader.getFontMetrics(
					Font(effectFont.name, it.toDouble()))::computeStringWidth
			},
			{
				Toolkit.getToolkit().fontLoader.getFontMetrics(
					Font(effectFont.name, it.toDouble()))::getLineHeight
			}
		)

		val h = Toolkit.getToolkit().fontLoader.getFontMetrics(
			Font(effectFont.name,
				multilineText.size.toDouble()))::getLineHeight



		for (i in multilineText.lines.indices) {
			gc.apply {
				fill = Color.BLACK
				textAlign = TextAlignment.LEFT
				font = effectFont.copy(size = multilineText.size.toDouble()).toFxFont()
			}.fillText(multilineText.lines[i], effectRect.x,
				effectRect.y + ((i + 1) * h()).toDouble(),
				effectRect.w)
		}


		rootPane.center = canvas

		canvas.setOnMouseMoved { infoText.text = "(${it.x}, ${it.y})" }

		return Result.success()
	}

}

private fun me.nekomatamune.ygomaker.Font.toFxFont(): Font {
	val weight = if (this.bold) FontWeight.BOLD else FontWeight.NORMAL
	val posture = if (this.italic) FontPosture.ITALIC else FontPosture.REGULAR
	return Font.font(this.name, weight, posture, this.size)
}


private fun GraphicsContext.fillText(text: String, rect: Rect) {
	this.fillText(text, rect.x, rect.y + rect.h, rect.w)
}