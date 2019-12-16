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

		gc.setFont(p.nameFont)
		gc.fill = getCardNameColor(card)
		gc.textAlign = TextAlignment.LEFT
		gc.fillText(card.name, p.nameRect.x, p.nameRect.y + p.nameRect.h,
			p.nameRect.w)


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
						gc.setFont(p.spellTrapTypeFont)
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
			val image = Image(imagePath.toUri().toString())


			gc.drawImage(image, it.x.toDouble(), it.y.toDouble(), it.size.toDouble(),
				it.size.toDouble(),
				p.imageRect.x, p.imageRect.y, p.imageRect.w,
				p.imageRect.h)
		}


		if (card.type.isMonster()) {
			val text = getMonsterTypeText(card)
			gc.fill = Color.BLACK
			gc.setFont(p.monsterTypeFont)
			gc.textAlign = TextAlignment.LEFT
			gc.fillText(text, p.monsterTypeRect.x, p.monsterTypeRect.y)

			gc.setFont(p.atkDefFont)
			gc.fillText("ATK/${card.monster!!.atk}", p.atkRect.x, p.atkRect.y)
			gc.fillText("DEF/${card.monster!!.def}", p.defRect.x, p.defRect.y)
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

		gc.fill = Color.BLACK
		gc.setFont(effectFont.copy(size = multilineText.size.toDouble()))
		gc.textAlign = TextAlignment.LEFT

		for (i in multilineText.lines.indices) {
			gc.fillText(multilineText.lines[i], effectRect.x,
				effectRect.y + ((i + 1) * h()).toDouble(),
				effectRect.w)
		}


		rootPane.center = canvas

		canvas.setOnMouseMoved { infoText.text = "(${it.x}, ${it.y})" }

		return Result.success()
	}

}

private fun GraphicsContext.setFont(font: me.nekomatamune.ygomaker.Font) {
	val weight = if (font.bold) FontWeight.BOLD else FontWeight.NORMAL
	val posture = if (font.italic) FontPosture.ITALIC else FontPosture.REGULAR
	this.font = Font.font(font.name, weight, posture, font.size)
}