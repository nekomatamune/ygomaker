package me.nekomatamune.ygomaker.fx

import javafx.scene.image.Image
import me.nekomatamune.ygomaker.Result
import strikt.api.Assertion
import strikt.api.expectThat

fun <T> Result<T>.assertSuccess() {
	expectThat(this).isSuccess()
}

fun <T> Assertion.Builder<Result<T>>.isSuccess() =
		assert("is success") {
			if (it.isSuccess()) pass() else fail()
		}

fun <T> Assertion.Builder<Result<T>>.isFailure() =
		assert("is failure") {
			if (it.isFailure()) pass() else fail()
		}

fun Assertion.Builder<Image>.isPixelEqualTo(expected: Image) =
		assert("is pixel equal to") {
			val actualPixels = it.pixelReader
			val expectedPixels = expected.pixelReader

			(0 until it.width.toInt()).forEach { i ->
				(0 until it.height.toInt()).forEach { j ->
					val actualColor = actualPixels.getColor(i, j)
					val expectColor = expectedPixels.getColor(i, j)
					if (actualColor != expectColor) {
						fail("pixel at ($i, $j): $actualColor != $expectColor")
					}
				}
			}

			pass()
		}

