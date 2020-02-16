package me.nekomatamune.ygomaker.fx

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
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

	beforeEachTest {
		ctrl.injectFileChooserFactoryForTesting { mockFileChooser }
		ctrl.setImageModifiedHandler {
			success()
		}

		runFx {
			ctrl.setState(someImage, TEST_PACK_DIR)
		}.assertSuccess()
	}

	group("#setState") {

		test("Should populate UI components") {
			val expectedImage = someImage.copy(
					x = 22, y = 44, size = 66, file = "500x500.jpg")
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
				ctrl.setState(someImage.copy(file = ""))
			}.let {
				expectThat(it).isSuccess()
			}

			robot.lookupAs<ImageView>("#imageView").let {
				expectThat(it.image).isNull()
			}
		}
	}

	group("#onClickFileTextField") {

		test("Should not draw image when no image is selected") {
			val myImage = someImage.copy(file = "500x500.jpg")
			val myFxImage = FxImage(
					TEST_PACK_DIR.resolve(myImage.file).toUri().toString())

			every {
				mockFileChooser.showOpenDialog(any())
			} returns (null)

			runFx {
				ctrl.setState(myImage, TEST_PACK_DIR)
			}.assertSuccess()
			robot.clickOn("#fileTextField")

			verify {
				mockFileChooser.showOpenDialog(any())
			}

			robot.lookupAs<TextField>("#fileTextField").let {
				expectThat(it.text).isEqualTo(myImage.file)
			}
			robot.lookupAs<ImageView>("#imageView").let {
				expectThat(it.image).isPixelEqualTo(myFxImage)
			}
		}

		test("Should draw image when new image is selected") {
			val myImage = someImage.copy(size = 250)
			val expectedImageFileBasename = "250x250.jpg"
			val expectedImageFile = TEST_PACK_DIR.resolve(expectedImageFileBasename)
			val expectedFxImage = FxImage(expectedImageFile.toUri().toString())

			every {
				mockFileChooser.showOpenDialog(any())
			} returns (expectedImageFile.toFile())

			runFx {
				ctrl.setState(myImage, TEST_PACK_DIR)
			}.assertSuccess()
			robot.clickOn("#fileTextField")

			verify {
				mockFileChooser.showOpenDialog(any())
			}

			robot.lookupAs<TextField>("#fileTextField").let {
				expectThat(it.text).isEqualTo(expectedImageFileBasename)
			}

			robot.lookupAs<ImageView>("#imageView").let {
				expectThat(it.fitWidth.toInt()).isEqualTo(myImage.size)
				expectThat(it.image).isPixelEqualTo(expectedFxImage)
			}
		}
	}

	group("#onSpinnerValueChange") {

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

	// TODO: add unit test for mouse drag, scroll, and zoom


})