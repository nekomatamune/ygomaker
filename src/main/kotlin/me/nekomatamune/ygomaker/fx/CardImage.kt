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
import me.nekomatamune.ygomaker.Image
import me.nekomatamune.ygomaker.success
import me.nekomatamune.ygomaker.toAbsNormPath
import mu.KotlinLogging
import org.jetbrains.annotations.TestOnly
import java.io.FileNotFoundException
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.math.max
import kotlin.math.roundToInt

private val logger = KotlinLogging.logger { }

/**
 * Controller for fx/CardImage.fxml.
 */
open class CardImage {

	// region FXML components
	@FXML private lateinit var fileTextField: TextField
	@FXML private lateinit var xSpinner: Spinner<Int>
	@FXML private lateinit var ySpinner: Spinner<Int>
	@FXML private lateinit var sizeSpinner: Spinner<Int>
	@FXML private lateinit var imageView: ImageView
	@FXML private lateinit var imageHBox: HBox
	// endregion

	// region Controller states
	private var packDir: Path = Paths.get("")
	private var mouseClickX: Int = 0
	private var mouseClickY: Int = 0
	// endregion

	// region Other properties
	private var imageModifiedHandler: (Image) -> Unit = {
		logger.warn { "Handler not set!" }
	}
	private var fileChooserFactory = { FileChooser() }
	private val spinnerListenerLock = SoftLock()
	// endregion

	/**
	 * Called by the javafx framework when this component is first loaded.
	 *
	 * Sets up listeners for own FXML components.
	 */
	@FXML
	private fun initialize() {
		logger.debug { "Initializing CardImage" }

		sequenceOf(xSpinner, ySpinner, sizeSpinner).forEach {
			it.addSimpleListener { onSpinnerValueChanged() }
		}
		fileTextField.setOnMouseClicked { onClickFileText() }
		imageView.apply {
			setOnMousePressed { onMousePressed(it) }
			setOnMouseDragged { onMouseDragged(it) }
			setOnScroll { onMouseScrolled(it) }
			setOnZoom { onZoom(it) }
		}
	}

	/**
	 * Sets the [handler] to be invoked when a new image is selected by this
	 * component.
	 */
	fun setImageModifiedHandler(handler: (Image) -> Unit) {
		imageModifiedHandler = handler
	}

	fun setState(image: Image, packDir: Path): Result<Unit> {
		logger.info { "image=$image, packDir=$packDir" }

		fileTextField.text = image.file
		spinnerListenerLock.lockAndRun {
			xSpinner.valueFactory.value = image.x
			ySpinner.valueFactory.value = image.y
			sizeSpinner.valueFactory.value = image.size
		}

		this.packDir = packDir
		logger.info { "new packDir: ${this.packDir}" }

		if (image.file.isEmpty()) {
			logger.warn { "No image is given." }
			return Result.success()
		}

		return loadImage()
	}

	/**
	 * Injects a fake factory for [FileChooser] for testing purpose.
	 */
	@TestOnly
	fun injectFileChooserFactoryForTesting(factory: () -> FileChooser) {
		fileChooserFactory = factory
	}

	private fun onSpinnerValueChanged() {
		logger.trace { "onSpinnerValueChanged" }
		spinnerListenerLock.runIfNotLocked {
			updateViewPort()
			dispatchModifyCardImageEvent()
		}
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

	private fun onClickFileText() {
		logger.debug { "onClickFileText()" }

		fileChooserFactory().apply {
			title = "Select an Image File"
			initialDirectory = packDir.toFile()
			extensionFilters.add(
					FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg"))

		}.showOpenDialog(null).let { selectedFile ->
			logger.debug { "Selected image file: $selectedFile" }
			logger.debug { "packDir: $packDir" }
			val imageFile = packDir.toAbsNormPath().relativize(selectedFile.toPath())

			logger.debug { "Relativized image file: $imageFile" }

			setState(Image(
					file = imageFile.toString()
			), packDir)

			dispatchModifyCardImageEvent()

		}
	}

	private fun dispatchModifyCardImageEvent() {
		imageModifiedHandler(Image(
				file = fileTextField.text,
				x = xSpinner.value,
				y = ySpinner.value,
				size = sizeSpinner.value
		))
	}

	private fun loadImage(): Result<Unit> {
		if (fileTextField.text.isBlank()) {
			imageView.image = null
			return Result.success()
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

			val image = javafx.scene.image.Image(it.toURI().toString())
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

		return Result.success()
	}
}