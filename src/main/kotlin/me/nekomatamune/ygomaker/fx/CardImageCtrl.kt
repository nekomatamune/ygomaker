@file:Suppress("NAME_SHADOWING")

package me.nekomatamune.ygomaker.fx

import javafx.fxml.FXML
import javafx.geometry.Rectangle2D
import javafx.scene.control.Spinner
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory
import javafx.scene.control.TextField
import javafx.scene.image.ImageView
import javafx.scene.input.MouseEvent
import javafx.scene.input.ScrollEvent
import javafx.scene.input.ZoomEvent
import javafx.scene.layout.HBox
import javafx.stage.FileChooser
import me.nekomatamune.ygomaker.FileIO
import me.nekomatamune.ygomaker.Image
import me.nekomatamune.ygomaker.Result
import me.nekomatamune.ygomaker.failure
import me.nekomatamune.ygomaker.success
import me.nekomatamune.ygomaker.toAbsNormPath
import mu.KotlinLogging
import org.jetbrains.annotations.TestOnly
import java.io.File
import java.io.FileNotFoundException
import java.nio.file.Path
import kotlin.math.min
import kotlin.math.roundToInt
import javafx.scene.image.Image as FxImage

private val logger = KotlinLogging.logger { }
private const val SCROLL_ZOOM_RATIO = 0.0001

/**
 * Controller for fx/CardImage.fxml.
 */
open class CardImageCtrl(
		val fileChooserFactory: () -> FileChooser = { FileChooser() },
		val fileIO: FileIO = FileIO()
) {

	// region FX components
	@FXML private lateinit var fileTextField: TextField
	@FXML private lateinit var xSpinner: Spinner<Int>
	@FXML private lateinit var ySpinner: Spinner<Int>
	@FXML private lateinit var sizeSpinner: Spinner<Int>
	@FXML private lateinit var imageView: ImageView
	@FXML private lateinit var imageHBox: HBox
	// endregion

	// region FX component-backed properties
	private var image: Image
		get() = Image(
				file = fileTextField.text,
				x = xSpinner.value,
				y = ySpinner.value,
				size = sizeSpinner.value
		)
		set(image) {
			handlerLock.lockAndRun {
				fileTextField.text = image.file
				xSpinner.valueFactory.value = image.x
				ySpinner.valueFactory.value = image.y
				sizeSpinner.valueFactory.value = image.size
				imageView.image.let {
					xSpinner.intValueFactory().max =
							if (it == null) 0 else it.width.toInt() - image.size
					ySpinner.intValueFactory().max =
							if (it == null) 0 else it.height.toInt() - image.size
				}
			}
		}

	private var fxImage: FxImage?
		get() = imageView.image
		set(value) {
			imageView.image = value
			handlerLock.lockAndRun {
				sizeSpinner.intValueFactory().max =
						if (value == null) 0 else min(value.height,
								value.width).roundToInt()
			}
		}

	private var fxImageViewport: Rectangle2D
		get() = imageView.viewport
		set(value) {
			imageView.viewport = value
		}
	// endregion

	// region Controller states
	var imageModifiedHandler: (Image) -> Result<Unit> = {
		failure(IllegalStateException("Handler not set"))
	}

	private lateinit var packDir: Path
	private lateinit var lastMousePressedEvent: MouseEvent
	private val handlerLock = HandlerLock()
	// endregion

	// region FX initializer
	@FXML
	private fun initialize() {
		logger.debug { "Initializing CardImage" }

		// Set event handlers
		sequenceOf(xSpinner, ySpinner, sizeSpinner).forEach {
			it.valueProperty().addListener(handlerLock) { oldValue, newValue ->
				onSpinnerValueChanged(oldValue, newValue).alertFailure()
			}
		}
		fileTextField.setOnMouseClicked(handlerLock) {
			onClickFileText().alertFailure()
		}
		imageView.apply {
			setOnMousePressed(handlerLock) { onMousePressed(it) }
			setOnMouseDragged(handlerLock) { onMouseDragged(it).alertFailure() }
			setOnScroll(handlerLock) { onMouseScrolled(it).alertFailure() }
			setOnZoom(handlerLock) { onZoom(it).alertFailure() }
		}
	}
	// endregion

	// region Public API
	/**
	 * Sets the state, which will trigger changes on the own components.
	 */
	fun setState(newImage: Image, newPackDir: Path = packDir): Result<Unit> {
		val newPackDir = newPackDir.toAbsNormPath()
		logger.info { "image=$newImage, packDir=$newPackDir" }

		fxImage = if (newImage.file.isBlank()) {
			logger.info { "No image file is specified. Unload image." }
			null
		} else {
			val imageFile = newPackDir.resolve(newImage.file).toFile()
			fileIO.readImage(imageFile).onFailure {
				return it
			}
		}

		image = newImage
		packDir = newPackDir
		fxImageViewport = image.toViewport()
		return success()
	}
	// endregion

	//region FX component handlers
	/**
	 * Invoked when the value of [xSpinner], [ySpinner], or [sizeSpinner] changes.
	 */
	private fun onSpinnerValueChanged(oldValue: Int,
			newValue: Int): Result<Unit> {
		logger.trace { "onSpinnerValueChanged(): $oldValue -> $newValue" }
		fxImageViewport = image.toViewport()

		return imageModifiedHandler(image)
	}

	private fun onMousePressed(event: MouseEvent) {
		logger.trace { "onMousePressed(): $event" }
		lastMousePressedEvent = event
	}

	private fun onMouseDragged(event: MouseEvent): Result<Unit> {
		logger.trace { "onMouseDragged(): $event" }

		val scale = sizeSpinner.value / imageHBox.prefHeight
		image = image.copy(
				x = (image.x + (lastMousePressedEvent.screenX - event.screenX) * scale).roundToInt(),
				y = (image.y + (lastMousePressedEvent.screenY - event.screenY) * scale).roundToInt()
		)
		fxImageViewport = image.toViewport()
		lastMousePressedEvent = event

		return imageModifiedHandler(image)
	}

	private fun onMouseScrolled(event: ScrollEvent): Result<Unit> {
		logger.trace { "onMouseScrolled(): $event" }

		image = image.copy(
				size = (image.size * (1 + (event.deltaY * SCROLL_ZOOM_RATIO))).roundToInt()
		)
		fxImageViewport = image.toViewport()

		return imageModifiedHandler(image)
	}

	private fun onZoom(event: ZoomEvent): Result<Unit> {
		logger.trace { "onZoom(): $event" }

		image = image.copy(
				size = (image.size / event.zoomFactor).roundToInt()
		)
		fxImageViewport = image.toViewport()

		return imageModifiedHandler(image)
	}

	/**
	 * Opens a [FileChooser] dialog for user to select an image file.
	 */
	private fun onClickFileText(): Result<Unit> {
		logger.debug { "onClickFileText()" }

		val chooser = fileChooserFactory().apply {
			title = "Select an Image File"
			initialDirectory = packDir.toFile()
			extensionFilters += FileChooser.ExtensionFilter(
					"Image Files", "*.png", "*.jpg")
		}

		val imageFile: File? = chooser.showOpenDialog(null)
				.also { logger.info { "Selected image file: $it" } }

		if (imageFile == null) {
			logger.warn { "No image file selected. Most likely canceled by user." }
			return success()
		}

		val newFxImage = fileIO.readImage(imageFile).onFailure {
			return it
		}.also {
			logger.info { "Image read successfully!" }
		}

		image = Image(
				file = packDir.relativize(imageFile.toPath()).toString(),
				x = 0,
				y = 0,
				size = min(newFxImage.width, newFxImage.height).roundToInt()
		)
		fxImage = newFxImage
		fxImageViewport = image.toViewport()

		return imageModifiedHandler(image)
	}
	//endregion
}

// region Helper functions

private fun readImageFile(file: File): Result<FxImage> {
	logger.debug { "Reading image from file: $file" }

	if (!file.exists()) {
		return failure(FileNotFoundException(file.toString()))
	}
	if (!file.isFile) {
		return failure(IllegalArgumentException("Not a file: $file"))
	}

	return try {
		val image = FxImage(file.toURI().toString())
		success(image)
	} catch (error: Exception) {
		failure(error)
	}
}

private fun Image.toViewport() =
		Rectangle2D(x.toDouble(), y.toDouble(), size.toDouble(), size.toDouble())

private fun Spinner<Int>.intValueFactory() =
		(valueFactory as IntegerSpinnerValueFactory)

// endregion