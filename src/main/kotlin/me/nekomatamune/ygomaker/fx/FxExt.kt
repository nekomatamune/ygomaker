package me.nekomatamune.ygomaker.fx

import javafx.beans.value.ObservableValue
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.input.MouseDragEvent
import javafx.scene.input.MouseEvent
import javafx.scene.input.ScrollEvent
import javafx.scene.input.ZoomEvent
import me.nekomatamune.ygomaker.Result
import mu.KotlinLogging

private val logger = KotlinLogging.logger { }




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



fun <T> Result<T>.alertFailure() {
	if (this.isFailure()) {
		logger.error(this.error()) { "Got Exception:" }
		Alert(Alert.AlertType.ERROR, "${this.error()}", ButtonType.OK).showAndWait()
	}
}