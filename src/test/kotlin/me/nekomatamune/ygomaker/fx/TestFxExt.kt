package me.nekomatamune.ygomaker.fx

import com.google.common.io.Resources
import javafx.application.Application
import javafx.application.Platform
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.Parent
import javafx.stage.Stage
import javafx.util.Callback
import org.spekframework.spek2.dsl.Root
import org.testfx.api.FxRobot
import org.testfx.api.FxToolkit
import java.util.concurrent.Semaphore
import kotlin.reflect.KClass

fun <C> Root.setupTestFx(
		fxmlLocation: String,
		controllers: Map<KClass<*>, () -> Any>
) {

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
				FxToolkit.registerPrimaryStage()
				FxToolkit.setupApplication {
					object : Application() {
						override fun start(primaryStage: Stage) {
							primaryStage.scene = javafx.scene.Scene(loader.load<Parent>())
							primaryStage.show()
						}
					}
				}
			},
			destructor = {
				FxToolkit.cleanupApplication(it)
				FxToolkit.cleanupStages()
			}
	)

	val ctrl by memoized<C> {
		app
		loader.getController()
	}

	val robot by memoized {
		ctrl
		FxRobot()
	}
}

fun tearDownFx() {
	if (FxToolkit.isFXApplicationThreadRunning()) {
		Platform.exit()
	}
}

fun runFx(block: () -> Unit) {
	Platform.runLater {
		block()
	}

	val semaphore = Semaphore(0)
	Platform.runLater {
		semaphore.release()
	}
	semaphore.acquire()
}

fun <T> FxRobot.lookupAs(id: String, clazz: KClass<T>): T where T : Node {
	return this.lookup(id).queryAs(clazz.java)
}

