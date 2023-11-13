/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.locators

import kotlinx.kover.gradle.plugin.sources.LanguageCompilationSource
import kotlinx.kover.gradle.plugin.sources.VariantCompilationSource
import kotlinx.kover.gradle.plugin.util.DynamicBean
import org.gradle.api.Task
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.tasks.TaskProvider
import java.io.File

internal fun Iterable<DynamicBean>.jvmCompilations(
    isJavaOutput: (File) -> Boolean
): Map<String, VariantCompilationSource> {
    return associate { compilation ->
        val name = compilation.value<String>("name")
        name to extractJvmCompilation(compilation, isJavaOutput)
    }
}

private fun extractJvmCompilation(
    compilation: DynamicBean,
    isJavaOutput: (File) -> Boolean
): VariantCompilationSource {
    val sources = compilation.beanCollection("allKotlinSourceSets").flatMap<DynamicBean, File> {
        it["kotlin"].valueCollection("srcDirs")
    }.toSet()

    val kotlinOutputs = compilation["output"].value<ConfigurableFileCollection>("classesDirs").files.filterNot<File> {
        isJavaOutput(it)
    }.toSet()

    val javaOutputs = compilation["output"].value<ConfigurableFileCollection>("classesDirs").files.filter<File> {
        isJavaOutput(it)
    }.toSet()

    val kotlinCompileTask = compilation.value<Task>("compileKotlinTask")
    val javaCompileTask = compilation.valueOrNull<TaskProvider<Task>?>("compileJavaTaskProvider")?.orNull

    val kotlin = LanguageCompilationSource(kotlinOutputs, kotlinCompileTask)
    val java = LanguageCompilationSource(javaOutputs, javaCompileTask)

    return VariantCompilationSource(sources, kotlin, java)
}
