package me.nekomatamune.ygomaker.fx

import javafx.fxml.FXML
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import javafx.stage.DirectoryChooser
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import me.nekomatamune.ygomaker.Backupper
import me.nekomatamune.ygomaker.Command
import me.nekomatamune.ygomaker.Pack
import me.nekomatamune.ygomaker.deepCopyTo
import mu.KotlinLogging
import java.io.FileNotFoundException
import java.nio.file.Files
import java.nio.file.Path

private val logger = KotlinLogging.logger { }
private val json = Json(JsonConfiguration.Stable.copy(prettyPrint = true))

class Window {
	@FXML lateinit var menuBarController: MenuBar
	@FXML lateinit var cardListController: CardListController
	@FXML lateinit var cardRendererController: CardRendererController
	@FXML lateinit var cardFormController: CardFormController
	private val backupper by lazy {
		Backupper(Command.dataDir.resolve("bak"), 10)
	}
	private lateinit var packDir: Path

	@FXML
	fun initialize() {
		logger.info { "Init Window..." }

		packDir = Command.dataDir.resolve(Command.packCode)

		menuBarController.menuActionHandler = {
			when (it) {
				MenuAction.LOAD_PACK -> loadPack()
				MenuAction.SAVE_PACK -> savePack()
				MenuAction.SAVE_PACK_AS -> savePackAs()
				MenuAction.NEW_CARD -> cardListController.newCard()
				MenuAction.RENDER_CARD -> cardRendererController.render()
			}
		}

		cardListController.cardSelectedHandler = {
			cardFormController.setCard(it, packDir)
			cardRendererController.setCard(it)
			cardRendererController.render()
		}

		cardFormController.cardModifiedHandler = {
			cardListController.onModifyCard(it)
			cardRendererController.setCard(it)
		}

		loadPack(Command.dataDir.resolve(Command.packCode))
	}

	private fun loadPack(packDir: Path? = null) {
		val packDir = packDir ?: DirectoryChooser().apply {
			title = "Select a pack directory"
			initialDirectory = Command.dataDir.toFile()
		}.showDialog(null).toPath()

		val cardFile = packDir.resolve("pack.json")
		if (!cardFile.toFile().isFile) {
			throw FileNotFoundException(cardFile.toString())
		}

		val pack = json.parse(Pack.serializer(), cardFile.toFile().readText())
		cardListController.setPack(pack)
		this.packDir = packDir
	}

	private fun savePack() {
		logger.info { "Saving pack into $packDir" }
		val cardFile = packDir.resolve("pack.json")

		backupper.backup(cardFile)

		val packJson = json.stringify(Pack.serializer(),
			cardListController.getPack())

		cardFile.toFile().writeText(packJson)
	}

	private fun savePackAs() {
		logger.debug { "onSavePackAsMenuItem()" }

		val newPackDir = DirectoryChooser().apply {
			title = "Enter or select a new pack directory"
			initialDirectory = Command.dataDir.toFile()
		}.showDialog(null).toPath()

		if (Files.exists(newPackDir)) {
			Alert(Alert.AlertType.CONFIRMATION).apply {
				headerText = "Overwriting existing pack..."
				contentText = "This will overwrite the existing pack ${newPackDir.fileName}. Proceed?"
			}.showAndWait().filter(ButtonType.OK::equals).ifPresent {
				logger.info { "Writing pack to ${newPackDir.fileName}" }

				packDir.deepCopyTo(newPackDir)
				this.packDir = newPackDir

				savePack()
			}
		}
	}
}