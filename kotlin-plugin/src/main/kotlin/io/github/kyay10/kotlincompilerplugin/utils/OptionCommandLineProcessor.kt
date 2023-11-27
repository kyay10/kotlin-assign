/*
 * Copyright (C) 2021 Youssef Shoaib
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
@file:Suppress("unused")

package io.github.kyay10.kotlincompilerplugin.utils

import kotlin.reflect.KProperty
import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CliOptionProcessingException
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey

@OptIn(ExperimentalCompilerApi::class)
open class OptionCommandLineProcessor(override val pluginId: String) : CommandLineProcessor {
  override val pluginOptions: MutableCollection<TransformableCliOption<*>> = mutableListOf()

  operator fun <T : Any> TransformableCliOption<T>.provideDelegate(
    thisRef: Any?,
    property: KProperty<*>
  ): TransformableCliOption<T> = this.also { pluginOptions.add(it) }

  operator fun <T : Any> TransformableCliOption<T>.getValue(
    thisRef: Any?,
    property: KProperty<*>
  ): CompilerConfigurationKey<T> = configurationKey

  override fun processOption(
    option: AbstractCliOption,
    value: String,
    configuration: CompilerConfiguration
  ) {
    (option as? TransformableCliOption<Any>)?.let {
      configuration.put(it.configurationKey,  it.transform(value))
    } ?: throw CliOptionProcessingException("Unknown plugin option $option")
  }
}

data class TransformableCliOption<out T : Any>(
    override val optionName: String,
    override val valueDescription: String,
    override val description: String,
    override val required: Boolean = true,
    override val allowMultipleOccurrences: Boolean = false,
    val transform: (String) -> T,
) : AbstractCliOption {
  val configurationKey: CompilerConfigurationKey<@UnsafeVariance T> = CompilerConfigurationKey<T>(optionName)
}

fun option(
    optionName: String,
    valueDescription: String,
    description: String,
    required: Boolean = true,
    allowMultipleOccurrences: Boolean = false,
): TransformableCliOption<String> =
    option(optionName, valueDescription, description, required, allowMultipleOccurrences) { it }
fun <T : Any> option(
  optionName: String,
  valueDescription: String,
  description: String,
  required: Boolean = true,
  allowMultipleOccurrences: Boolean = false,
  transform: (String) -> T,
): TransformableCliOption<T> =
    TransformableCliOption(
        optionName, valueDescription, description, required, allowMultipleOccurrences, transform)