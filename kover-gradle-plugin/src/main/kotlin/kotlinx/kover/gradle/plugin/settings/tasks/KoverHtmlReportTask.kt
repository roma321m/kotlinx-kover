/*
 * Copyright 2017-2024 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.settings.tasks

import kotlinx.kover.features.jvm.ClassFilters
import kotlinx.kover.features.jvm.KoverFeatures
import kotlinx.kover.features.jvm.KoverLegacyFeatures
import kotlinx.kover.gradle.plugin.commons.ArtifactContent
import kotlinx.kover.gradle.plugin.settings.artifacts.ArtifactSerializer
import kotlinx.kover.gradle.plugin.settings.artifacts.ProjectArtifactInfo
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Provider
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.*

@CacheableTask
internal abstract class KoverHtmlReportTask : DefaultTask() {
    // relative sensitivity for file collections which are not FileTree is a comparison by file name and its contents
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val artifacts: ConfigurableFileCollection

    @get:Input
    abstract val includedProjects: SetProperty<String>
    @get:Input
    abstract val excludedProjects: SetProperty<String>

    @get:Input
    abstract val excludedClasses: SetProperty<String>

    @get:Nested
    val data: Provider<Map<String, ProjectArtifactInfo>> = artifacts.elements.map { elements ->
        elements.map { location -> location.asFile }
            .map { file -> ArtifactSerializer.deserialize(file.bufferedReader(), rootDir) }
            .map(::filterProjectSources)
            .associateBy { it.path }
    }

    @get:OutputDirectory
    abstract val htmlDir: DirectoryProperty

    private val rootDir = project.rootDir

    @TaskAction
    fun generate() {
        val artifacts = data.get().values

        val reports = artifacts.flatMap { artifact -> artifact.reports }
        val sources = artifacts.flatMap { artifact -> artifact.compilations.filter { it.key != "test" }.values }
            .flatMap { compilation -> compilation.sourceDirs }
        val outputs = artifacts.flatMap { artifact -> artifact.compilations.filter { it.key != "test" }.values }
            .flatMap { compilation -> compilation.outputDirs }

        val excluded = excludedClasses.get()

        KoverLegacyFeatures.generateHtmlReport(
            htmlDir.asFile.get(),
            null,
            reports,
            outputs,
            sources,
            "Prototype HTML report",
            ClassFilters(emptySet(), excluded, emptySet())
        )
    }

    private fun filterProjectSources(info: ProjectArtifactInfo): ProjectArtifactInfo {
        val included = includedProjects.get()
        val excluded = excludedProjects.get()

        if (included.isNotEmpty()) {
            val notIncluded = included.none { filter ->
                KoverFeatures.koverWildcardToRegex(filter).toRegex().matches(info.path)
            }
            if (notIncluded) {
                return ProjectArtifactInfo(info.path, info.reports, emptyMap())
            }
        }

        if (excluded.isNotEmpty()) {
            val excl = excluded.any { filter ->
                KoverFeatures.koverWildcardToRegex(filter).toRegex().matches(info.path)
            }
            if (excl) {
                return ProjectArtifactInfo(info.path, info.reports, emptyMap())
            }
        }
        return info
    }
}