/*
 * Copyright 2017-2024 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.settings.tasks

import kotlinx.kover.gradle.plugin.tools.*
import kotlinx.kover.gradle.plugin.tools.kover.KoverTool
import org.gradle.api.DefaultTask
import org.gradle.api.file.*
import org.gradle.api.tasks.*
import javax.inject.*

/**
 * Task to get online instrumentation agent jar file by specified coverage tool.
 *
 * The task is cached, so in general there should not be a performance issue on large projects.
 */
@CacheableTask
internal abstract class KoverAgentSearchTask : DefaultTask() {
    // relative sensitivity for file collections which are not FileTree is a comparison by file name and its contents
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val agentClasspath: ConfigurableFileCollection

    @get:OutputFile
    abstract val agentJar: RegularFileProperty

    @get:Inject
    protected abstract val archiveOperations: ArchiveOperations

    private val tool = KoverTool(KoverToolBuiltin)

    @TaskAction
    fun find() {
        val srcJar = tool.findJvmAgentJar(agentClasspath, archiveOperations)
        srcJar.copyTo(agentJar.get().asFile, true)
    }
}
