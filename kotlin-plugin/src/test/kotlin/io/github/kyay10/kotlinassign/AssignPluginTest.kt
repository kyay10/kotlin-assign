/*
 * Copyright (C) 2020 Brian Norman
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
@file:OptIn(ExperimentalCompilerApi::class)

package io.github.kyay10.kotlinassign

import com.tschuchort.compiletesting.JvmCompilationResult
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import java.io.*
import java.lang.reflect.InvocationTargetException
import kotlin.system.measureTimeMillis
import kotlin.test.Test

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AssignPluginTest {
  private val outStream = ByteArrayOutputStream()
  private val sampleFiles = mutableListOf<SourceFile>()
  private lateinit var compiledSamples: JvmCompilationResult

  @BeforeAll
  fun setupSampleFiles() {
    val sampleJvmMainDirectory = File(BuildConfig.SAMPLE_JVM_MAIN_PATH)
    sampleFiles.addAll(
        sampleJvmMainDirectory
            .listFilesRecursively { it.extension == "kt" || it.extension == "java" }
            .map { SourceFile.fromPath(it) })
    println(
        "Kotlin Sample Compilation took ${
                measureTimeMillis {
                    compiledSamples = compileSources(sampleFiles, outStream)
                }
            } milliseconds")
    outStream.writeTo(System.out)
  }

  @Test
  fun `works for simple assignment`() {
    val output = compiledSamples.runMain()
    assert(output == "42\n")
  }

  @Test
  fun `works for vectors`() {
    val output = compiledSamples.runMain("Vec2Kt")
    assert(output == "Vec2(x=3.0, y=4.0)\n")
  }
}

private fun JvmCompilationResult.runMain(className: String = "MainKt"): String {
  val realOut = System.out
  val myOut = ByteArrayOutputStream()
  val printStream = PrintStream(myOut, false)
  System.setOut(printStream)
  val kClazz = classLoader.loadClass(className)
  val main = kClazz.declaredMethods.single { it.name == "main" && it.parameterCount == 0 }
  try {
    main.invoke(null)
    printStream.flush()
    return myOut.toString()
  } catch (e: InvocationTargetException) {
    throw e.targetException!!
  } finally {
    System.setOut(realOut)
  }
}

@OptIn(ExperimentalCompilerApi::class)
private fun compileSources(
    sourceFiles: List<SourceFile>,
    outputStream: OutputStream,
) =
    KotlinCompilation()
        .apply {
          supportsK2 = true
          sources = sourceFiles
          compilerPluginRegistrars = listOf(AssignPluginRegistrar())
          commandLineProcessors = listOf(AssignCommandLineProcessor())
          inheritClassPath = true
          messageOutputStream = outputStream
          verbose = true
          languageVersion = "2.0"
          kotlincArguments += "-Xallow-kotlin-package"
          kotlincArguments += "-Xcontext-receivers"
        }
        .compile()

fun File.listFilesRecursively(filter: FileFilter): List<File> =
    listOf(this).listFilesRecursively(filter, mutableListOf())

tailrec fun List<File>.listFilesRecursively(
    filter: FileFilter,
    files: MutableList<File> = mutableListOf()
): List<File> {
  val dirs = mutableListOf<File>()
  for (file in this) {
    val filteredFiles = file.listFiles(filter) ?: continue
    files.addAll(filteredFiles)
    dirs.addAll(file.listFiles { it: File -> it.isDirectory } ?: continue)
  }
  return dirs
      .ifEmpty {
        return files
      }
      .listFilesRecursively(filter, files)
}