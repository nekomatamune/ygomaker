package me.nekomatamune.ygomaker.fx

import com.google.common.io.Resources
import javafx.application.Application
import javafx.application.Platform
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.layout.GridPane
import javafx.stage.Stage
import org.spekframework.spek2.Spek
import org.testfx.api.FxRobot
import org.testfx.api.FxToolkit


object CardFormControllerSpec : Spek({
	lateinit var app: Application
	lateinit var ctrl: CardFormController
	lateinit var pane: GridPane

	beforeGroup {
		FxToolkit.registerPrimaryStage()
	}

	afterGroup {
		FxToolkit.cleanupStages()
		if (FxToolkit.isFXApplicationThreadRunning()) {
			Platform.exit()
		}
	}

	beforeEachTest {
		app = FxToolkit.setupApplication {
			object : Application() {
				override fun start(primaryStage: Stage) {
					FXMLLoader().apply {
						location = Resources.getResource("fx/CardForm.fxml")
					}.let {
						pane = it.load()
						ctrl = it.getController()
					}

					primaryStage.scene = Scene(pane)
					primaryStage.show()
				}
			}
		}
	}

	afterEachTest {
		FxToolkit.cleanupApplication(app)
	}



	test("Should do a test") {


		val robot = FxRobot()
		robot.rightClickOn("#cardNameTextField")
		robot.write("Hello World!")

	}

	test("Should do another test") {


	}

})
