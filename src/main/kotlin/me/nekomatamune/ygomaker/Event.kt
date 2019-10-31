package me.nekomatamune.ygomaker

import javafx.event.ActionEvent
import mu.KotlinLogging
import java.nio.file.Path

typealias EventHandler = (Event) -> Result<Unit>

data class Event(
	val name: EventName = EventName.UNKNOWN,

	// Standard JavaFX events
	val actionEvent: ActionEvent? = null,

	// The following fields are payloads of various EventName
	val packDir: Path? = null,
	val card: Card? = null,
	val image: Image? = null
)

enum class EventName {
	UNKNOWN,
	LOAD_PACK,
	SAVE_PACK,
	SAVE_PACK_AS,
	SELECT_CARD,
	MODIFY_CARD,
	MODIFY_CARD_IMAGE,
}

