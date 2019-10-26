package me.nekomatamune.ygomaker.fx

import javafx.event.Event
import javafx.event.EventHandler
import javafx.scene.control.*

fun Control.addSimpleListener(listener: () -> Unit) {
	when (this) {
		is TextInputControl -> this.textProperty()
		is ComboBoxBase<*> -> this.valueProperty()
		is CheckBox -> this.selectedProperty()
		is ListView<*> -> this.selectionModel.selectedItemProperty()
		is Spinner<*> -> this.valueProperty()
		else -> error("Unexpected control class ${this.javaClass}")
	}.addListener { _, _, _ -> listener() }
}

fun <T : Event> (() -> Unit).asEventHandler() = EventHandler<T> { this() }
fun <T : Event> ((T) -> Unit).asEventHandler() = EventHandler<T> { this(it) }
