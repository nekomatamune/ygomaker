package me.nekomatamune.ygomaker.fx

import javafx.beans.value.ObservableValue

fun <T> ObservableValue<T>.addSimpleListener(listener: () -> Unit) {
	this.addListener { _, _, _ -> listener() }
}