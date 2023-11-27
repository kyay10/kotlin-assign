package io.github.kyay10.kotlinassign

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KMutableProperty0

fun <T> KMutableProperty0<T>.assign(value: T) {
  set(value)
}

fun <T> ReadWriteProperty<Any?, T>.assign(value: T) {
  var temp by this
  temp = value
}