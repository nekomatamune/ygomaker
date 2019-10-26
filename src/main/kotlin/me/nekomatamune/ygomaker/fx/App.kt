package me.nekomatamune.ygomaker.fx

import com.google.common.io.Resources
import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.layout.BorderPane
import javafx.stage.Stage
import mu.KotlinLogging


private val logger = KotlinLogging.logger { }

class App : Application() {
	override fun start(primaryStage: Stage) {

		logger.info { "Loading AppPane" }
		val appPane = FXMLLoader().apply {
			location = Resources.getResource("fx/Window.fxml")
		}.load<BorderPane>()

		logger.info { "Showing stage" }
		primaryStage.apply {
			title = "YGOMaker"
			scene = Scene(appPane)
		}.show()
	}
}
