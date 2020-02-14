package me.nekomatamune.ygomaker.fx

import javafx.beans.value.ObservableValue
import javafx.scene.Node
import javafx.scene.input.MouseDragEvent
import javafx.scene.input.MouseEvent
import javafx.scene.input.ScrollEvent
import javafx.scene.input.ZoomEvent

class HandlerLock {
	private var locked = false

	fun runIfNotLocked(block: () -> Unit) {
		if (!locked) block()
	}

	fun lockAndRun(block: () -> Unit) {
		locked = true
		block()
		locked = false
	}
}

fun Node.setOnMouseClicked(lock: HandlerLock, handler: (MouseEvent) -> Unit) {
	setOnMouseClicked { lock.runIfNotLocked { handler(it) } }
}

fun Node.setOnMousePressed(lock: HandlerLock, handler: (MouseEvent) -> Unit) {
	setOnMousePressed { lock.runIfNotLocked { handler(it) } }
}

fun Node.setOnMouseDragged(
		lock: HandlerLock,
		handler: (MouseEvent) -> Unit
) {
	setOnMouseDragged { lock.runIfNotLocked { handler(it) } }
}

fun Node.setOnScroll(lock: HandlerLock, handler: (ScrollEvent) -> Unit) {
	setOnScroll { lock.runIfNotLocked { handler(it) } }
}

fun Node.setOnZoom(lock: HandlerLock, handler: (ZoomEvent) -> Unit) {
	setOnZoom { lock.runIfNotLocked { handler(it) } }
}

fun <T> ObservableValue<T>.addListener(
		lock: HandlerLock,
		listener: (T, T) -> Unit
) {
	addListener { _, oldValue: T, newValue: T ->
		lock.runIfNotLocked { listener(oldValue, newValue) }
	}
}