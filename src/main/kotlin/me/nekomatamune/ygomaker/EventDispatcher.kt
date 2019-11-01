package me.nekomatamune.ygomaker

import mu.KotlinLogging

private val logger = KotlinLogging.logger { }

/**
 * A singleton [EventDispatcher]. The default instance to use for all event
 * dispatching and handling.
 */
val dispatcher = EventDispatcher()

/**
 * Dispatches [Event] by registering and invoking [EventHandler].
 */
class EventDispatcher {
	private val handlers = mutableMapOf<EventType, MutableList<EventHandler>>()

	fun register(type: EventType, handler: EventHandler) {
		handlers.getOrPut(type, ::mutableListOf).add(handler)
	}

	fun dispatch(event: Event): Result<Unit> {
		logger.trace { "Dispatching event ${event.type}" }

		val res = handlers[event.type]
			?.map { it(event) }
			?.filter { it.isFailure }
			?.onEach {
				it.exceptionOrNull()?.let { error ->
					logger.error(error) { "Handler fails with error" }
				}
			}
			?.firstOrNull()

		// TODO: Somehow `:? Result.success(Unit)` causes compilation error.
		// Investigate why.
		@Suppress("IfThenToElvis")
		return if (res != null) res else Result.success()
	}
}