/*
 * Copyright 2017-2024 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.settings.instrumentation

import kotlinx.kover.gradle.plugin.commons.CoverageToolVendor.KOVER
import kotlinx.kover.gradle.plugin.commons.binReportPath
import kotlinx.kover.gradle.plugin.tools.KoverToolBuiltin
import kotlinx.kover.gradle.plugin.tools.kover.KoverTool
import org.gradle.api.Named
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.*
import org.gradle.api.tasks.testing.Test
import org.gradle.process.CommandLineArgumentProvider
import java.io.File


internal object JvmOnFlyInstrumenter {

    /**
     * Add online instrumentation to all JVM test tasks.
     */
    fun instrument(
        tasks: TaskCollection<Test>,
        jarConfiguration: Configuration,
        filter: InstrumentationFilter
    ) {
        tasks.configureEach {
            val binReportProvider =
                project.layout.buildDirectory.map { dir ->
                    dir.file(binReportPath(name, KOVER))
                }

            doFirst {
                // delete report so that when the data is re-measured, it is not appended to an already existing file
                // see https://github.com/Kotlin/kotlinx-kover/issues/489
                binReportProvider.get().asFile.delete()
            }

            // Always excludes android classes, see https://github.com/Kotlin/kotlinx-kover/issues/89
            val androidClasses = setOf(
                // Always excludes android classes, see https://github.com/Kotlin/kotlinx-kover/issues/89
                "android.*", "com.android.*",
                // excludes JVM internal classes, in some cases, errors occur when trying to instrument these classes, for example, when using JaCoCo + Robolectric. There is also no point in instrumenting them in Kover.
                "jdk.internal.*"
            )

            val excludedClassesWithAndroid = filter.copy(excludes = filter.excludes + androidClasses)


            // TODO exclude specified tasks
            if (true) {
                dependsOn(jarConfiguration)
                jvmArgumentProviders += JvmTestTaskArgumentProvider(
                    temporaryDir,
                    project.objects.fileCollection().from(jarConfiguration),
                    excludedClassesWithAndroid,
                    binReportProvider
                )
            }
        }
    }
}

data class InstrumentationFilter(
    @get:Input
    val includes: Set<String>,
    @get:Input
    val excludes: Set<String>
)


/**
 * Provider of additional JVM string arguments for running a test task.
 */
private class JvmTestTaskArgumentProvider(
    private val tempDir: File,

    // relative sensitivity for file is a comparison by file name and its contents
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    val jarFiles: ConfigurableFileCollection,

    @get:Nested
    val filter: InstrumentationFilter,

    @get:OutputFile
    val reportProvider: Provider<RegularFile>
) : CommandLineArgumentProvider, Named {

    @Internal
    override fun getName(): String {
        return "koverArgumentsProvider"
    }

    override fun asArguments(): MutableIterable<String> {
        val files = jarFiles.files
        if (files.size != 1) {
            println("AGENT FILES COUNT: ${files.size}")
            return mutableSetOf()
        }
        val jarFile = files.single()

        return KoverTool(KoverToolBuiltin)
            .jvmAgentArgs(jarFile, tempDir, reportProvider.get().asFile, filter.excludes)
            .toMutableList()
    }
}
