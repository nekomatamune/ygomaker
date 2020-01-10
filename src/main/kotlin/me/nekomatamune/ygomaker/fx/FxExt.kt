package me.nekomatamune.ygomaker.fx

import javafx.scene.control.*

/**
 * Wraps [listener] into a [javafx.beans.value.ChangeListener] to listens to
 * value changed event of [this].
 *
 * [listener] is expected to get the updated value from the receiver itself
 * directly.
 */
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

class SoftLock {
	private var locked = false

	fun runIfNotLocked(block: () -> Unit) {
		if (!locked) {
			block()
		}
	}

	fun lockAndRun(block: () -> Unit) {
		locked = true
		block()
		locked = false
	}
}