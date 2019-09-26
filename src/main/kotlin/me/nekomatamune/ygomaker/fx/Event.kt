package me.nekomatamune.ygomaker.fx

import me.nekomatamune.ygomaker.Card
import javafx.event.ActionEvent
import mu.KotlinLogging
import java.nio.file.Path

private val logger = KotlinLogging.logger { }


private val handlers = mutableMapOf<EventName, MutableList<EventHandler>>()

fun registerEventHandler(name: EventName, handler: EventHandler) {
	handlers.getOrPut(name, ::mutableListOf).add(handler)
}

fun unregisterAllEventHandlers() {
	handlers.clear()
}

fun dispatchEvent(event: Event) {
	logger.debug { "Dispatching event ${event.name}" }

	handlers[event.name]?.forEach {
		it(event).onFailure {
			logger.error(it.cause) { "Handler fails with error: ${it.cause}" }
		}
	}
}

typealias EventHandler = (Event) -> Result<Unit>

data class Event(
	val name: EventName,

	// Standard JavaFX events
	val actionEvent: ActionEvent? = null,

	// The following fields are payloads of various EventName
	val packDir: Path? = null,
	val card: Card? = null
)

enum class EventName {
	UNKNOWN,
	LOAD_PACK,
	SAVE_PACK,
	SAVE_PACK_AS,
	SELECT_CARD,
	MODIFY_CARD,
}

