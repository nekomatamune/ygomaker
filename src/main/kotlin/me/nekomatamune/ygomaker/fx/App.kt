package me.nekomatamune.ygomaker.fx

import com.google.common.io.Resources
import javafx.application.Application
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.layout.BorderPane
import javafx.scene.text.Text
import javafx.stage.Stage
import me.nekomatamune.ygomaker.Command
import me.nekomatamune.ygomaker.Event
import me.nekomatamune.ygomaker.EventName
import me.nekomatamune.ygomaker.dispatcher
import mu.KotlinLogging

private val logger = KotlinLogging.logger { }

class App : Application() {

	override fun start(primaryStage: Stage) {

		logger.info { "Loading App..." }
		val appPane = FXMLLoader().apply {
			location = Resources.getResource("fx/Window.fxml")
		}.load<BorderPane>()

		dispatcher.dispatch(Event(
			name = EventName.LOAD_PACK,
			packDir = Command.dataDir.resolve(Command.packCode)
		))

		logger.info { "Showing stage" }
		primaryStage.apply {
			title = "YGOMaker"
			scene = Scene(appPane)
		}.show()
	}
}
