package me.nekomatamune.ygomaker.fx

import com.google.common.io.Resources
import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.layout.BorderPane
import javafx.stage.Stage
import mu.KotlinLogging

private val logger = KotlinLogging.logger { }

class YGOMakerApp : Application() {

	override fun start(primaryStage: Stage) {

		logger.info { "Loading App..." }
		val appPane = FXMLLoader().apply {
			location = Resources.getResource("fx/App.fxml")
		}.load<BorderPane>()

		logger.info { "Showing stage" }
		primaryStage.apply {
			title = "YGOMaker"
			scene = Scene(appPane)
		}.show()
	}
}
