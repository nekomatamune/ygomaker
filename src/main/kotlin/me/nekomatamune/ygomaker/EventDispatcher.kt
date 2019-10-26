package me.nekomatamune.ygomaker

import mu.KotlinLogging

private val logger = KotlinLogging.logger { }

class EventDispatcher {
	private val handlers = mutableMapOf<EventName, MutableList<EventHandler>>()

	fun register(name: EventName, handler: EventHandler) {
		handlers.getOrPut(name, ::mutableListOf).add(handler)
	}

	fun dispatch(event: Event) {
		logger.trace { "Dispatching event ${event.name}" }

		handlers[event.name]?.forEach {
			it(event).onFailure {
				logger.error(it.cause) { "Handler fails with error: ${it.cause}" }
			}
		}
	}
}