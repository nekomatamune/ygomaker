package me.nekomatamune.ygomaker.fx

import io.mockk.every
import io.mockk.mockk
import javafx.scene.control.Spinner
import javafx.scene.control.TextField
import javafx.scene.image.ImageView
import javafx.stage.FileChooser
import me.nekomatamune.ygomaker.Image
import me.nekomatamune.ygomaker.toAbsNormPath
import org.spekframework.spek2.Spek
import org.testfx.api.FxRobot
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.nio.file.Paths
import javafx.scene.image.Image as FxImage


object CardImageSpec : Spek({
	setupTestFx<CardImage>(
			fxmlLocation = "fx/CardImage.fxml",
			controllers = mapOf(CardImage::class to { CardImage() })
	)

	val ctrl by memoized<CardImage>()
	val robot by memoized<FxRobot>()
	val mockFileChooser by memoized { mockk<FileChooser>(relaxed = true) }

	val testPackDir = Paths.get(
			"src", "test", "resources", "fx", "TEST"
	).toAbsNormPath()

	lateinit var selectedImage: Image
	beforeEachTest {
		ctrl.fileChooserFactory = { mockFileChooser }
		ctrl.imageModifiedHandler = { selectedImage = it.copy() }
	}

	afterGroup { tearDownFx() }

	test("Should populate fields when set with image.") {
		val expectedImage = Image(
				x = 12, y = 34, size = 250, file = "250x250.jpg"
		)

		runFx {
			ctrl.setState(expectedImage, testPackDir)
		}

		expectThat(
				robot.lookup("#fileTextField").queryAs(TextField::class.java).text
		).isEqualTo(expectedImage.file)
		expectThat(
				robot.lookup("#xSpinner").queryAs(Spinner::class.java).value
		).isEqualTo(expectedImage.x)
		expectThat(
				robot.lookup("#ySpinner").queryAs(Spinner::class.java).value
		).isEqualTo(expectedImage.y)
		expectThat(
				robot.lookup("#sizeSpinner").queryAs(Spinner::class.java).value
		).isEqualTo(expectedImage.size)
	}

	test("Should draw image in view port when new image is selected.") {
		val expectedImageSize = 250
		val expectedImageFileBasename = "250x250.jpg"
		val expectedImageFile = testPackDir.resolve(expectedImageFileBasename)

		every {
			mockFileChooser.showOpenDialog(any())
		} returns (expectedImageFile.toFile())

		//region action
		runFx {
			ctrl.setState(Image(), testPackDir)
		}

		robot.rightClickOn("#fileTextField")
		//endregion

		val imageView = robot.lookup("#imageView").queryAs(ImageView::class.java)
		expectThat(imageView.fitWidth.toInt()).isEqualTo(expectedImageSize)

		expectThat(
				robot.lookup("#fileTextField").queryAs(TextField::class.java).text
		).isEqualTo(expectedImageFileBasename)

		val actualImage = imageView.image
		val expectedImage = FxImage(expectedImageFile.toUri().toString())
		val actualPixels = actualImage.pixelReader
		val expectedPixels = expectedImage.pixelReader
		(0 until actualImage.width.toInt()).forEach { i ->
			(0 until actualImage.height.toInt()).forEach { j ->
				expectThat(actualPixels.getArgb(i, j))
						.describedAs("Pixel at ($i,$j)")
						.isEqualTo(expectedPixels.getArgb(i, j))
			}
		}

	}
})