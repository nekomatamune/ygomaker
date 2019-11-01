package me.nekomatamune.ygomaker

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.output.CliktHelpFormatter
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.counted
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.path
import com.google.common.io.Resources
import javafx.application.Application.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import me.nekomatamune.ygomaker.fx.App
import mu.KotlinLogging
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.core.config.Configurator
import java.nio.file.Path
import java.nio.file.Paths

private val logger = KotlinLogging.logger { }

object Command : CliktCommand(
	name = "java -jar ygomaker.jar",
	help = "Display the content of PACK."
) {

	val packCode: String by argument(
		name = "PACK",
		help = """The codename of the pack codename to display (e.g. TEST). 
			|There must be a directory with this codename under DATA_DIR, 
			|containing a "pack.json" file.
			|""".trimMargin())

	val noGui: Boolean by option(
		"--no-gui", metavar = "NO_GUI_MODE", help = "Do not show the GUI."
	).flag(default = false)

	val dataDir: Path by option(
		"--data_dir", metavar = "DATA_DIR", help = "The input data directory"
	).path(exists = true, fileOkay = false, readable = true
	).default(value = Paths.get(".", "data"))

	private val verbose: Int by option(
		"-v", help = "Increase verbosity"
	).counted()

	init {
		context { helpFormatter = CliktHelpFormatter(showDefaultValues = true) }
	}

	override fun run() {
		when (verbose) {
			1 -> Configurator.setRootLevel(Level.INFO)
			2 -> Configurator.setRootLevel(Level.DEBUG)
			in 3..Int.MAX_VALUE -> Configurator.setRootLevel(Level.TRACE)
		}

		Resources.getResource("banner.txt").readText().lines().forEach(logger::info)

		if (noGui) {
			val pack = Json(JsonConfiguration.Stable).parse(Pack.serializer(),
				dataDir.resolve(packCode).resolve("pack.json").toFile().readText())
			echo(pack)
			return
		}

		launch(App::class.java)
	}
}

