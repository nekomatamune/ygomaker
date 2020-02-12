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
import kotlin.math.min
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
		failure(IllegalStateException("Handler not set!"))
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
			it.addSimpleListener { onSpinnerValueChanged().alertFailure() }
		}
		fileTextField.setOnMouseClicked { onClickFileText() }
		imageView.apply {
			setOnMousePressed { onMousePressed(it).alertFailure() }
			setOnMouseDragged { onMouseDragged(it).alertFailure() }
			setOnScroll { onMouseScrolled(it).alertFailure() }
			setOnZoom { onZoom(it).alertFailure() }
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
	fun setState(newImage: Image, newPackDir: Path = packDir): Result<Unit> {
		logger.info { "image=$newImage, packDir=$newPackDir" }

		packDir = newPackDir.toAbsNormPath()
		logger.info { "Normalized packDir: $packDir" }

		fileTextField.text = newImage.file

		// Lock to avoid triggering handlers that cyclically call this method.
		spinnerListenerLock.lockAndRun {
			xSpinner.valueFactory.value = newImage.x
			ySpinner.valueFactory.value = newImage.y
			sizeSpinner.valueFactory.value = newImage.size
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
		logger.trace { "onSpinnerValueChanged()" }

		spinnerListenerLock.runIfNotLocked {
			refreshViewPort().onFailure { return it }
			invokeImageModifiedHandler().onFailure { return it }
		}

		return success()
	}

	private fun onMousePressed(event: MouseEvent): Result<Unit> {
		logger.debug { "onMousePressed(): $event" }
		mouseClickX = event.screenX.roundToInt()
		mouseClickY = event.screenY.roundToInt()
		return success()
	}

	private fun onMouseDragged(event: MouseEvent): Result<Unit> {
		logger.trace { "onMouseDragged(): $event" }

		val scale = sizeSpinner.value / imageHBox.prefHeight
		xSpinner.valueFactory.value =
				(xSpinner.value + (mouseClickX - event.screenX) * scale).roundToInt()
		ySpinner.valueFactory.value =
				(ySpinner.value + (mouseClickY - event.screenY) * scale).roundToInt()

		onMousePressed(event).onFailure { return it }
		return invokeImageModifiedHandler()
	}

	private fun onMouseScrolled(event: ScrollEvent): Result<Unit> {
		logger.trace { "onMouseScrolled(): $event" }
		sizeSpinner.valueFactory.value =
				(sizeSpinner.value * (1 + (event.deltaY * 0.001))).roundToInt()

		refreshViewPort().onFailure { return it }
		return invokeImageModifiedHandler()
	}

	private fun onZoom(event: ZoomEvent): Result<Unit> {
		logger.trace { "onZoom(): $event" }
		sizeSpinner.valueFactory.value =
				(sizeSpinner.value / event.zoomFactor).roundToInt()

		refreshViewPort().onFailure { return it }
		return invokeImageModifiedHandler()
	}

	/**
	 * Opens a [FileChooser] dialog for user to select an image file.
	 *
	 * Populates FX components accordingly and invokes [imageModifiedHandler].
	 */
	private fun onClickFileText(): Result<Unit> {
		logger.debug { "onClickFileText()" }

		val chooser = fileChooserFactory().apply {
			title = "Select an Image File"
			initialDirectory = packDir.toFile()
			extensionFilters.add(
					FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg")
			)
		}

		val selectedFile = chooser.showOpenDialog(null)
		logger.debug { "Selected image file: $selectedFile" }

		val imageFile = packDir
				.relativize(selectedFile.toPath())
		logger.debug { "Relativized image file: $imageFile" }

		setState(Image(file = imageFile.toString())).onFailure {
			return it
		}
		return invokeImageModifiedHandler()
	}
	//endregion

	/**
	 * Loads image.
	 */
	private fun loadImage(): Result<Unit> {
		if (fileTextField.text.isBlank()) {
			logger.warn { "No image file is specified. Unload existing image." }
			imageView.image = null
			return success()
		}

		val imageFile = packDir.resolve(fileTextField.text).normalize().toFile()
		logger.info { "Loading image from $imageFile" }

		if (!imageFile.exists()) {
			return failure(FileNotFoundException(imageFile.toString()))
		}
		if (!imageFile.isFile) {
			return failure(IllegalArgumentException("Not a file: $imageFile"))
		}

		val image = javafx.scene.image.Image(imageFile.toURI().toString())
		logger.info { "Image loaded." }

		imageView.image = image
		spinnerListenerLock.lockAndRun {
			(sizeSpinner.valueFactory as IntegerSpinnerValueFactory).max =
					min(image.height, image.width).toInt()
			(xSpinner.valueFactory as IntegerSpinnerValueFactory).max = image.width.toInt()
			(ySpinner.valueFactory as IntegerSpinnerValueFactory).max = image.height.toInt()
		}

		return refreshViewPort()
	}

	/**
	 * Invokes [imageModifiedHandler] with contents from the FX components.
	 */
	private fun invokeImageModifiedHandler(): Result<Unit> {
		logger.info { "invoke" }
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
		logger.info { "refreshViewPOrt" }
		imageView.viewport = Rectangle2D(
				xSpinner.value.toDouble(),
				ySpinner.value.toDouble(),
				sizeSpinner.value.toDouble(),
				sizeSpinner.value.toDouble())

		return success()
	}


}