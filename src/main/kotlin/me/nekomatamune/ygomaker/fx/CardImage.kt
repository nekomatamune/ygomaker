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
import me.nekomatamune.ygomaker.*
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

	private var imageModifiedHandler: (Image) -> Result<Unit> = {
		Result.failure(IllegalStateException("Handler not set!"))
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
			it.addSimpleListener { onSpinnerValueChanged().logFailure() }
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
	fun setImageModifiedHandler(handler: (Image) -> Result<Unit>) {
		imageModifiedHandler = handler
	}

	/**
	 * Sets the state, which will trigger changes on the own components.
	 */
	fun setState(newImage: Image, newPackDir: Path): Result<Unit> {
		logger.info { "image=$newImage, packDir=$newPackDir" }

		packDir = newPackDir
		fileTextField.text = newImage.file

		// Lock to avoid triggering handlers that cyclically call this method.
		spinnerListenerLock.lockAndRun {
			xSpinner.valueFactory.value = newImage.x
			ySpinner.valueFactory.value = newImage.y
			sizeSpinner.valueFactory.value = newImage.size
		}

		newImage.file.ifEmpty {
			logger.warn { "No new image is set." }
			return Result.ok()
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

	//region Internal listeners

	/**
	 * Invoked when the value of [xSpinner], [ySpinner], or [sizeSpinner] changes.
	 */
	private fun onSpinnerValueChanged(): Result<Unit> {
		logger.trace { "onSpinnerValueChanged" }
		return spinnerListenerLock.runIfNotLocked {
			refreshViewPort().then {
				invokeImageModifiedHandler()
			}
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
		invokeImageModifiedHandler()
	}

	private fun onMouseScrolled(event: ScrollEvent) {
		logger.trace { "Handling ScrollEvent: $event" }
		sizeSpinner.valueFactory.value =
				(sizeSpinner.value * (1 + (event.deltaY * 0.001))).roundToInt()
		refreshViewPort()
		invokeImageModifiedHandler()
	}

	private fun onZoom(event: ZoomEvent) {
		logger.trace { "Handling ZoomEvent: $event" }
		sizeSpinner.valueFactory.value =
				(sizeSpinner.value / event.zoomFactor).roundToInt()
		refreshViewPort()
		invokeImageModifiedHandler()
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

			invokeImageModifiedHandler()

		}
	}


	private fun loadImage(): Result<Unit> {
		if (fileTextField.text.isBlank()) {
			imageView.image = null
			return Result.ok()
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

		return refreshViewPort()
	}
	//endregion

	/**
	 * Invokes [imageModifiedHandler] with contents from the FX components.
	 */
	private fun invokeImageModifiedHandler(): Result<Unit> {
		return imageModifiedHandler(Image(
				file = fileTextField.text,
				x = xSpinner.value,
				y = ySpinner.value,
				size = sizeSpinner.value
		))
	}

	/**
	 * Updates [imageView]'s viewport according to the FX components.
	 */
	private fun refreshViewPort(): Result<Unit> {
		imageView.viewport = Rectangle2D(
				xSpinner.value.toDouble(),
				ySpinner.value.toDouble(),
				sizeSpinner.value.toDouble(),
				sizeSpinner.value.toDouble())

		return Result.ok()
	}


}