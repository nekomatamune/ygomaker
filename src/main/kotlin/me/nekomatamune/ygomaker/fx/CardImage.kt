package me.nekomatamune.ygomaker.fx

import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.geometry.Rectangle2D
import javafx.scene.control.Spinner
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory
import javafx.scene.control.TextField
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.MouseEvent
import javafx.scene.input.ScrollEvent
import javafx.scene.input.ZoomEvent
import javafx.scene.layout.HBox
import javafx.stage.FileChooser
import mu.KotlinLogging
import java.io.FileNotFoundException
import java.nio.file.Path
import kotlin.math.max
import kotlin.math.roundToInt
import me.nekomatamune.ygomaker.Image as CardImage

private val logger = KotlinLogging.logger { }

class CardImage {

	private lateinit var packDir: Path
	private var mouseClickX: Int = 0
	private var mouseClickY: Int = 0
	@FXML private lateinit var fileTextField: TextField
	@FXML private lateinit var xSpinner: Spinner<Int>
	@FXML private lateinit var ySpinner: Spinner<Int>
	@FXML private lateinit var sizeSpinner: Spinner<Int>
	@FXML private lateinit var imageView: ImageView
	@FXML private lateinit var imageHBox: HBox

	@FXML
	private fun initialize() {
		logger.debug { "Initializing CardImage" }

		sequenceOf(xSpinner, ySpinner, sizeSpinner).forEach {
			it.valueProperty().addListener { _, oldValue, newValue ->
				onSpinnerValueChange(oldValue, newValue)
			}
		}
		fileTextField.onMouseClicked = EventHandler { onClickImageFile() }
		imageView.onMousePressed = EventHandler<MouseEvent> { onMousePressed(it) }
		imageView.onMouseDragged = EventHandler<MouseEvent> { onMouseDragged(it) }
		imageView.onScroll = EventHandler<ScrollEvent> { onMouseScrolled(it) }
		imageView.onZoom = EventHandler<ZoomEvent> { onZoom(it) }

		registerEventHandler(EventName.SELECT_CARD, this::onSelectCard)
	}

	private fun onSpinnerValueChange(oldValue: Int, newValue: Int) {
		logger.trace { "onSpinnerValueChange: $oldValue -> $newValue" }
		updateViewPort()
		dispatchModifyCardImageEvent()
	}

	private fun onMousePressed(event: MouseEvent) {
		logger.trace { "Handling MouseEvent: $event" }
		mouseClickX = event.screenX.roundToInt()
		mouseClickY = event.screenY.roundToInt()
	}

	private fun onMouseDragged(event: MouseEvent) {
		logger.trace { "Handling MouseEvent: $event" }

		val scale = sizeSpinner.value / imageHBox.prefHeight

		xSpinner.valueFactory.value =
			(xSpinner.value + (mouseClickX - event.screenX) * scale).roundToInt()
		ySpinner.valueFactory.value =
			(ySpinner.value + (mouseClickY - event.screenY) * scale).roundToInt()
		onMousePressed(event)
		dispatchModifyCardImageEvent()
	}

	private fun onMouseScrolled(event: ScrollEvent) {
		logger.trace { "Handling ScrollEvent: $event" }
		sizeSpinner.valueFactory.value =
			(sizeSpinner.value * (1 + (event.deltaY * 0.001))).roundToInt()
		updateViewPort()
		dispatchModifyCardImageEvent()
	}

	private fun onZoom(event: ZoomEvent) {
		logger.trace { "Handling ZoomEvent: $event" }
		sizeSpinner.valueFactory.value =
			(sizeSpinner.value / event.zoomFactor).roundToInt()
		updateViewPort()
		dispatchModifyCardImageEvent()
	}

	private fun onSelectCard(event: Event): Result<Unit> {
		logger.debug { "Handling ${event.name} event" }

		(event.card?.image ?: CardImage()).let {
			fileTextField.text = it.file
			xSpinner.valueFactory.value = it.x
			ySpinner.valueFactory.value = it.y
			sizeSpinner.valueFactory.value = it.size
		}
		packDir = event.packDir!!.normalize().toAbsolutePath()
		return loadImage()
	}

	private fun onClickImageFile() {
		FileChooser().apply {
			title = "Select an Image File"
			initialDirectory = packDir.toFile()
			extensionFilters.add(
				FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg"))

		}.showOpenDialog(null).let { selectedFile ->
			logger.debug { "Selected image file: $selectedFile" }
			val imageFile = packDir.relativize(selectedFile.toPath())

			fileTextField.text = imageFile.toString()
			dispatchModifyCardImageEvent()
			loadImage().onFailure {
				logger.error(it.cause) { it.message }
			}
		}
	}

	private fun dispatchModifyCardImageEvent() {
		dispatchEvent(Event(
			name = EventName.MODIFY_CARD_IMAGE,
			image = me.nekomatamune.ygomaker.Image(
				file = fileTextField.text,
				x = xSpinner.value,
				y = ySpinner.value,
				size = sizeSpinner.value
			)
		))
	}

	private fun loadImage(): Result<Unit> {
		if(fileTextField.text.isBlank()) {
			imageView.image = null
			return Result.success(Unit)
		}

		val imagePath = packDir.resolve(fileTextField.text)
		logger.debug { "Loading image from ${imagePath.toUri()}" }

		imagePath.toFile().let {
			if (!it.exists()) {
				return Result.failure(FileNotFoundException(imagePath.toString()))
			}
			if (!it.isFile) {
				return Result.failure(
					IllegalArgumentException("Not a file: $it"))
			}
			logger.info { "Load here: $it" }

			val image = Image(it.toURI().toString())
			imageView.image = image
			(sizeSpinner.valueFactory as IntegerSpinnerValueFactory).max =
				max(image.height, image.width).toInt()
			(xSpinner.valueFactory as IntegerSpinnerValueFactory).max = image.width.toInt()
			(ySpinner.valueFactory as IntegerSpinnerValueFactory).max = image.height.toInt()
		}

		return updateViewPort()
	}

	private fun updateViewPort(): Result<Unit> {
		imageView.viewport = Rectangle2D(
			xSpinner.value.toDouble(),
			ySpinner.value.toDouble(),
			sizeSpinner.value.toDouble(),
			sizeSpinner.value.toDouble())

		return Result.success(Unit)
	}
}