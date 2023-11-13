/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.sources

import kotlinx.kover.gradle.plugin.commons.AndroidFallbacks
import kotlinx.kover.gradle.plugin.commons.AndroidFlavor
import org.gradle.api.Task
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskCollection
import org.gradle.api.tasks.testing.Test
import java.io.File


// TODO can it be merged with custom variant?
internal interface VariantSource {
    val tests: TaskCollection<Test>

    val compilations: Provider<Map<String, VariantCompilationSource>>
}

internal class VariantCompilationSource(
    /**
     * Directories of sources, used in [compileTasks].
     */
    val sources: Set<File>,

    val kotlin: LanguageCompilationSource,

    val java: LanguageCompilationSource,
)

internal class LanguageCompilationSource(
    /**
     * Directories with compiled classes, outputs of [compileTasks].
     */
    val outputs: Set<File>,

    /**
     * In case when no one compile tasks will be triggered,
     * output dirs will be empty and reporter can't determine project classes.
     *
     * So compile tasks must be triggered anyway.
     */
    val compileTask: Task?
)

internal class JvmVariantSource(
    override val tests: TaskCollection<Test>,
    override val compilations: Provider<Map<String, VariantCompilationSource>>,
) : VariantSource

internal class AndroidVariantSource(
    override val tests: TaskCollection<Test>,
    override val compilations: Provider<Map<String, VariantCompilationSource>>,
    val details: AndroidDetails
) : VariantSource

internal class AndroidDetails(
    val buildVariant: String,
    val buildType: String,
    val flavors: List<AndroidFlavor>,

    val fallbacks: AndroidFallbacks,

    /**
     * The flavors used in case the dependency contains a dimension that is missing in the current project.
     * Specific only for this build variant.
     *
     * map of (dimension > flavor)
     */
    val missingDimensions: Map<String, String>
)

internal class VariantSources(val jvm: JvmVariantSource?, val android: List<AndroidVariantSource>)
