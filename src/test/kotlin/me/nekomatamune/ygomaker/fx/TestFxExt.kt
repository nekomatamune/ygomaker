package me.nekomatamune.ygomaker.fx

import com.google.common.io.Resources
import javafx.application.Application
import javafx.application.Platform
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.Parent
import javafx.stage.Stage
import javafx.util.Callback
import mu.KotlinLogging
import org.spekframework.spek2.dsl.Root
import org.testfx.api.FxRobot
import org.testfx.api.FxToolkit
import java.util.concurrent.Semaphore
import kotlin.reflect.KClass

private val logger = KotlinLogging.logger { }

@Suppress("UNUSED_VARIABLE", "UNUSED_EXPRESSION")
fun <C> Root.setupTestFx(
		fxmlLocation: String,
		controllers: Map<KClass<*>, () -> Any>
) {
	logger.info { "setupTextFx()" }

	val loader by memoized {
		FXMLLoader().apply {
			location = Resources.getResource(fxmlLocation)
			controllerFactory = Callback<Class<*>, Any> {
				when (it.kotlin) {
					in controllers -> (controllers[it.kotlin] ?: error("")).invoke()
					else -> throw UnsupportedOperationException(
							"Missing factory for controller $it")
				}
			}
		}
	}
	val app by memoized(
			factory = {
				FxToolkit.setupApplication {
					object : Application() {
						override fun start(primaryStage: Stage) {
							logger.debug { "Starting TestFx Application" }
							primaryStage.scene = javafx.scene.Scene(loader.load<Parent>())
							primaryStage.show()
						}
					}
				}
			},
			destructor = {
				logger.debug { "Stopping TextFx Application" }
				FxToolkit.cleanupApplication(it)
			}
	)

	val ctrl by memoized<C> {
		loader.getController()
	}

	val robot by memoized {
		FxRobot()
	}

	beforeGroup {
		FxToolkit.registerPrimaryStage()
	}

	beforeEachTest {
		logger.debug { "Loading TextFx Application..." }
		app
	}

	afterGroup {
		FxToolkit.cleanupStages()
		if (FxToolkit.isFXApplicationThreadRunning() && isRunByIntellij()) {
			Platform.exit()
		}
	}
}

fun <T> runFx(block: () -> T): T {
	var result: T? = null

	Platform.runLater {
		result = block()
	}

	Semaphore(0).let {
		Platform.runLater {
			it.release()
		}
		it.acquire()
	}

	return result!!
}

inline fun <reified T> FxRobot.lookupAs(id: String): T where T : Node {
	return this.lookup(id).queryAs(T::class.java)
}

private fun isRunByIntellij() =
		System.getenv("XPC_SERVICE_NAME")?.contains("intellij") ?: false
