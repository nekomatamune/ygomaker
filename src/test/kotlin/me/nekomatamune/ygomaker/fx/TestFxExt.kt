package me.nekomatamune.ygomaker.fx

import javafx.application.Application
import javafx.application.Platform
import javafx.scene.Parent
import javafx.stage.Stage
import javafx.util.Callback
import org.spekframework.spek2.dsl.Root
import org.testfx.api.FxRobot
import org.testfx.api.FxToolkit
import java.net.URL
import java.util.concurrent.Semaphore

fun <C> Root.setupTextFx(
	fxmlLocation: URL,
	ctrlFactory: ((Class<*>) -> Any)
) {

	val robot by memoized {
		FxRobot()
	}

	val loader by memoized {
		javafx.fxml.FXMLLoader().apply {
			location = fxmlLocation
			controllerFactory = Callback<Class<*>, Any>(ctrlFactory)
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
		val a = app
		loader.getController()
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
