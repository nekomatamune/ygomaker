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


	group("Setting initial state") {
		test("Should populate UI fields") {
			val expectedImage = Image(
					x = 12, y = 34, size = 56, file = "original.jpg"
			)

			runFx {
				ctrl.setState(expectedImage, testPackDir)
			}

			expectThat(
					robot.lookupAs("#fileTextField", TextField::class).text
			).isEqualTo(expectedImage.file)
			expectThat(
					robot.lookupAs("#xSpinner", Spinner::class).value
			).isEqualTo(expectedImage.x)
			expectThat(
					robot.lookupAs("#ySpinner", Spinner::class).value
			).isEqualTo(expectedImage.y)
			expectThat(
					robot.lookupAs("#sizeSpinner", Spinner::class).value
			).isEqualTo(expectedImage.size)

			val viewPort = robot.lookupAs("#imageView", ImageView::class).viewport
			expectThat(viewPort.minX.toInt()).isEqualTo(expectedImage.x)
			expectThat(viewPort.minY.toInt()).isEqualTo(expectedImage.y)
			expectThat(viewPort.width.toInt()).isEqualTo(expectedImage.size)
			expectThat(viewPort.height.toInt()).isEqualTo(expectedImage.size)
		}

		test("Should set image.") {
			val imageData = Image(file = "original.jpg")

			runFx {
				ctrl.setState(imageData, testPackDir)
			}

			compareImagesByPixel(
					robot.lookupAs("#imageView", ImageView::class).image,
					FxImage(testPackDir.resolve(imageData.file).toUri().toString())
			)
		}
	}

	test("Should invoke image handler") {
		// TODO
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

		val imageView = robot.lookupAs("#imageView", ImageView::class)
		expectThat(imageView.fitWidth.toInt()).isEqualTo(expectedImageSize)

		expectThat(
				robot.lookupAs("#fileTextField", TextField::class).text
		).isEqualTo(expectedImageFileBasename)

		compareImagesByPixel(imageView.image,
				FxImage(expectedImageFile.toUri().toString()))
	}
})