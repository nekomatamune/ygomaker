package me.nekomatamune.ygomaker.fx

import javafx.fxml.FXML
import javafx.scene.image.ImageView
import mu.KotlinLogging

private val logger = KotlinLogging.logger {  }

class CardRenderer {

	@FXML private lateinit var imageView: ImageView

	@FXML
	fun initialize() {
		logger.debug { "Initializing CardRenderer" }
	}

}