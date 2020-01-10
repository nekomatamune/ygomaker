package me.nekomatamune.ygomaker.fx

import io.mockk.every
import io.mockk.mockk
import javafx.scene.image.ImageView
import javafx.stage.FileChooser
import me.nekomatamune.ygomaker.Image
import me.nekomatamune.ygomaker.toAbsNormPath
import org.spekframework.spek2.Spek
import org.testfx.api.FxRobot
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.nio.file.Paths


object CardImageSpec : Spek({
	setupTestFx<CardImage>(
		fxmlLocation = "fx/CardImage.fxml",
		controllers = mapOf(CardImage::class to { CardImage() })
	)

	val ctrl by memoized<CardImage>()
	val robot by memoized<FxRobot>()
	val mockFileChooser by memoized { mockk<FileChooser>(relaxed = true) }

	val myPackDir = Paths.get("src", "test", "resources", "fx",
		"TEST").toAbsNormPath()

	lateinit var selectedImage: Image
	beforeEachTest {
		ctrl.fileChooserFactory = { mockFileChooser }
		ctrl.imageModifiedHandler = { selectedImage = it.copy() }
	}

	afterGroup { tearDownFx() }

	test("Should draw image in view port when new image is selected.") {
		val expectedImageSize = 250
		val expectImageFile = myPackDir.resolve("250x250.jpg")

		every {
			mockFileChooser.showOpenDialog(any())
		} returns (expectImageFile.toFile())

		//region action
		runFx {
			ctrl.setState(Image(), myPackDir)
		}

		robot.rightClickOn("#fileTextField")
		//endregion

		val imageView = robot.lookup("#imageView").queryAs(ImageView::class.java)
		expectThat(imageView.fitWidth.toInt()).isEqualTo(expectedImageSize)

		val actualImage = robot.capture(
			robot.lookup("#imageView").queryAs(ImageView::class.java)).image
		val expectedImage = javafx.scene.image.Image(
			expectImageFile.toUri().toString())

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