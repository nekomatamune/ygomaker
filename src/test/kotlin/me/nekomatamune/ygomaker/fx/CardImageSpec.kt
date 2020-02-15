package me.nekomatamune.ygomaker.fx

import io.mockk.every
import io.mockk.mockk
import javafx.scene.control.Spinner
import javafx.scene.control.TextField
import javafx.scene.image.ImageView
import javafx.scene.input.KeyCode.ENTER
import javafx.stage.FileChooser
import me.nekomatamune.ygomaker.Image
import me.nekomatamune.ygomaker.success
import me.nekomatamune.ygomaker.toAbsNormPath
import org.spekframework.spek2.Spek
import org.testfx.api.FxRobot
import strikt.api.expectThat
import strikt.assertions.isBlank
import strikt.assertions.isEqualTo
import strikt.assertions.isNull
import java.nio.file.Path
import java.nio.file.Paths
import javafx.scene.image.Image as FxImage


private val TEST_PACK_DIR: Path = Paths
		.get("src", "test", "resources", "fx", "TEST")
		.toAbsNormPath()

object CardImageSpec : Spek({
	setupTestFx<CardImageCtrl>(
			fxmlLocation = "fx/CardImage.fxml",
			controllers = mapOf(CardImageCtrl::class to { CardImageCtrl() })
	)

	val ctrl by memoized<CardImageCtrl>()
	val robot by memoized<FxRobot>()
	val mockFileChooser by memoized { mockk<FileChooser>(relaxed = true) }


	val someImage = Image(x = 12, y = 34, size = 56, file = "original.jpg")
	lateinit var selectedImage: Image
	beforeEachTest {
		ctrl.injectFileChooserFactoryForTesting { mockFileChooser }
		ctrl.setImageModifiedHandler {
			selectedImage = it.copy()
			success()
		}
	}

	group("#setState") {

		test("Should populate UI components") {
			val expectedImage = someImage.copy(
					x = 12, y = 34, size = 56, file = "original.jpg")
			val expectedFxImage = FxImage(
					TEST_PACK_DIR.resolve(expectedImage.file).toUri().toString())

			runFx {
				ctrl.setState(expectedImage, TEST_PACK_DIR)
			}.let {
				expectThat(it).isSuccess()
			}

			robot.lookupAs<TextField>("#fileTextField").let {
				expectThat(it.text).isEqualTo(expectedImage.file)
			}
			robot.lookupAs<Spinner<Int>>("#xSpinner").let {
				expectThat(it.value).isEqualTo(expectedImage.x)
			}
			robot.lookupAs<Spinner<Int>>("#ySpinner").let {
				expectThat(it.value).isEqualTo(expectedImage.y)
			}
			robot.lookupAs<Spinner<Int>>("#sizeSpinner").let {
				expectThat(it.value).isEqualTo(expectedImage.size)
			}
			robot.lookupAs<ImageView>("#imageView").viewport.let {
				expectThat(it.minX.toInt()).isEqualTo(expectedImage.x)
				expectThat(it.minY.toInt()).isEqualTo(expectedImage.y)
				expectThat(it.width.toInt()).isEqualTo(expectedImage.size)
				expectThat(it.height.toInt()).isEqualTo(expectedImage.size)
			}

			robot.lookupAs<ImageView>("#imageView").image.let {
				expectThat(it).isPixelEqualTo(expectedFxImage)
			}
		}

		test("Should clear UI components when file path is empty") {
			runFx {
				ctrl.setState(someImage.copy(file = ""), TEST_PACK_DIR)
			}.let {
				expectThat(it).isSuccess()
			}

			robot.lookupAs<ImageView>("#imageView").let {
				expectThat(it.image).isNull()
			}
		}
	}

	group("#onClickFileText") {

		beforeEachTest {
			runFx { ctrl.setState(Image(), TEST_PACK_DIR) }.assertSuccess()
		}

		test("Should not draw image when no image is selected") {
			every {
				mockFileChooser.showOpenDialog(any())
			} returns (null)

			robot.rightClickOn("#fileTextField")

			robot.lookupAs<TextField>("#fileTextField").let {
				expectThat(it.text).isBlank()
			}
			robot.lookupAs<ImageView>("#imageView").let {
				expectThat(it.image).isNull()
			}
		}

		test("Should draw image when new image is selected") {
			val expectedImageSize = 250
			val expectedImageFileBasename = "250x250.jpg"
			val expectedImageFile = TEST_PACK_DIR.resolve(expectedImageFileBasename)
			val expectedFxImage = FxImage(expectedImageFile.toUri().toString())

			every {
				mockFileChooser.showOpenDialog(any())
			} returns (expectedImageFile.toFile())

			robot.rightClickOn("#fileTextField")


			robot.lookupAs<TextField>("#fileTextField").let {
				expectThat(it.text).isEqualTo(expectedImageFileBasename)
			}

			robot.lookupAs<ImageView>("#imageView").let {
				expectThat(it.fitWidth.toInt()).isEqualTo(expectedImageSize)
				expectThat(it.image).isPixelEqualTo(expectedFxImage)
			}
		}
	}

	group("#onSpinnerValueChange") {
		beforeEachTest {
			runFx { ctrl.setState(someImage, TEST_PACK_DIR) }.assertSuccess()
		}

		test("Should shift viewport when X and Y spinners changes") {
			robot.doubleClickOn("#xSpinner").write("33").type(ENTER)
			robot.doubleClickOn("#ySpinner").write("44").type(ENTER)

			robot.lookupAs<ImageView>("#imageView").let {
				expectThat(it.viewport.minX.toInt()).isEqualTo(33)
				expectThat(it.viewport.minY.toInt()).isEqualTo(44)
			}
		}

		test("Should zoom viewport when size spinner changes") {
			robot.doubleClickOn("#sizeSpinner").write("55").type(ENTER)

			robot.lookupAs<ImageView>("#imageView").let {
				expectThat(it.viewport.width.toInt()).isEqualTo(55)
				expectThat(it.viewport.height.toInt()).isEqualTo(55)
			}
		}


	}


})