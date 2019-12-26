package me.nekomatamune.ygomaker

import javafx.event.ActionEvent
import java.nio.file.Path

typealias EventHandler = (Event) -> Result<Unit>

/**
 * Encapsulates the type and payloads of an event. What payloads are set depends
 * on the [Event.name].
 */
data class Event(
	val name: EventName = EventName.UNKNOWN,

	// The following fields are payloads of various EventName
	val packDir: Path? = null,
	val card: Card? = null,
	val image: Image? = null
)

/**
 * List of events that can be fired by the UI components.
 */
enum class EventName {
	UNKNOWN,
	/** Should also set [Event.packDir] */
	LOAD_PACK,
	/** Should also set [Event.packDir] */
	SAVE_PACK,
	/** Should also set [Event.packDir] */
	SAVE_PACK_AS,
	/** Should also set [Event.card] */
	SELECT_CARD,
	/** Should also set [Event.card] */
	MODIFY_CARD,
	/** Does not contain payload */
	RENDER,
	/** Does not contain payload */
	NEW_CARD,
}

