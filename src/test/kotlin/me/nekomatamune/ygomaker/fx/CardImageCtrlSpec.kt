package me.nekomatamune.ygomaker.fx

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import javafx.scene.control.Spinner
import javafx.scene.control.TextField
import javafx.scene.image.ImageView
import javafx.scene.input.KeyCode.ENTER
import javafx.stage.FileChooser
import me.nekomatamune.ygomaker.Image
import me.nekomatamune.ygomaker.Result
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


object CardImageCtrlSpec : Spek({
	val mockFileChooser by memoized { mockk<FileChooser>(relaxed = true) }

	setupTestFx<CardImageCtrl>(
			fxmlLocation = "fx/CardImage.fxml",
			controllers = mapOf(CardImageCtrl::class to {
				CardImageCtrl(fileChooserFactory = { mockFileChooser })
			})
	)

	val kTestPathDir: Path = Paths
			.get("src", "test", "resources", "fx", "TEST")
			.toAbsNormPath()
	val kSomeImage = Image(x = 12, y = 34, size = 56, file = "original.jpg")

	val ctrl by memoized<CardImageCtrl>()
	val robot by memoized<FxRobot>()

	val mockImageModifiedHandler = mockk<(Image) -> Result<Unit>>()
	val imageSlot = slot<Image>()

	beforeEachTest {
		ctrl.imageModifiedHandler = mockImageModifiedHandler
		every {
			mockImageModifiedHandler(any())
		}.returns(success())

		runFx {
			ctrl.setState(kSomeImage, kTestPathDir)
		}.assertSuccess()
	}

	group("#setState") {

		test("Should populate UI components") {
			val expectedImage = kSomeImage.copy(
					x = 22, y = 44, size = 66, file = "500x500.jpg")
			val expectedFxImage = FxImage(
					kTestPathDir.resolve(expectedImage.file).toUri().toString())

			runFx {
				ctrl.setState(expectedImage, kTestPathDir)
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
				ctrl.setState(kSomeImage.copy(file = ""))
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
			val myImage = kSomeImage.copy(file = "500x500.jpg")
			val myFxImage = FxImage(
					kTestPathDir.resolve(myImage.file).toUri().toString())

			every {
				mockFileChooser.showOpenDialog(any())
			} returns (null)

			// action
			runFx {
				ctrl.setState(myImage, kTestPathDir)
			}.assertSuccess()
			robot.clickOn("#fileTextField")

			// assert
			robot.lookupAs<TextField>("#fileTextField").let {
				expectThat(it.text).isEqualTo(myImage.file)
			}
			robot.lookupAs<ImageView>("#imageView").let {
				expectThat(it.image).isPixelEqualTo(myFxImage)
			}
			verify { mockFileChooser.showOpenDialog(any()) }
			verify(exactly = 0) { mockImageModifiedHandler(any()) }
		}

		test("Should draw image when new image is selected") {
			val myImage = kSomeImage.copy(size = 250)
			val expectedImageFileBasename = "250x250.jpg"
			val expectedImageFile = kTestPathDir.resolve(expectedImageFileBasename)
			val expectedFxImage = FxImage(expectedImageFile.toUri().toString())

			every {
				mockFileChooser.showOpenDialog(any())
			} returns (expectedImageFile.toFile())

			// action
			runFx {
				ctrl.setState(myImage, kTestPathDir)
			}.assertSuccess()
			robot.clickOn("#fileTextField")

			// assert
			verify { mockFileChooser.showOpenDialog(any()) }
			robot.lookupAs<TextField>("#fileTextField").let {
				expectThat(it.text).isEqualTo(expectedImageFileBasename)
			}
			robot.lookupAs<ImageView>("#imageView").let {
				expectThat(it.fitWidth.toInt()).isEqualTo(myImage.size)
				expectThat(it.image).isPixelEqualTo(expectedFxImage)
			}
			verify { mockImageModifiedHandler(capture(imageSlot)) }
			expectThat(imageSlot.captured.file)
					.isEqualTo(expectedImageFileBasename)
		}
	}

	group("#onSpinnerValueChange") {

		test("Should shift viewport when X and Y spinners changes") {
			val expectedX = 33
			val expectedY = 44

			// action
			robot.doubleClickOn("#xSpinner").write(expectedX.toString()).type(ENTER)
			robot.doubleClickOn("#ySpinner").write(expectedY.toString()).type(ENTER)

			// assert
			robot.lookupAs<ImageView>("#imageView").let {
				expectThat(it.viewport.minX.toInt()).isEqualTo(expectedX)
				expectThat(it.viewport.minY.toInt()).isEqualTo(expectedY)
			}
			verify { mockImageModifiedHandler(capture(imageSlot)) }
			imageSlot.captured.let {
				expectThat(it.x).isEqualTo(expectedX)
				expectThat(it.y).isEqualTo(expectedY)
			}
		}

		test("Should zoom viewport when size spinner changes") {
			val expectedSize = 55

			// action
			robot.doubleClickOn("#sizeSpinner")
					.write(expectedSize.toString()).type(ENTER)

			robot.lookupAs<ImageView>("#imageView").let {
				expectThat(it.viewport.width.toInt()).isEqualTo(expectedSize)
				expectThat(it.viewport.height.toInt()).isEqualTo(expectedSize)
			}
			verify { mockImageModifiedHandler(capture(imageSlot)) }
			imageSlot.captured.size.let { expectThat(it).isEqualTo(expectedSize) }
		}
	}

	// TODO: add unit test for mouse drag, scroll, and zoom


})