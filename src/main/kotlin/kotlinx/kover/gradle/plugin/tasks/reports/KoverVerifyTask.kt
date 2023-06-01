/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.tasks.reports

import kotlinx.kover.gradle.plugin.commons.VerificationRule
import kotlinx.kover.gradle.plugin.tools.CoverageTool
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject

@CacheableTask
internal abstract class KoverVerifyTask @Inject constructor(tool: CoverageTool) : AbstractKoverReportTask(tool) {
    @get:Nested
    abstract val rules: ListProperty<VerificationRule>

    @get:OutputFile
    abstract val resultFile: RegularFileProperty

    @TaskAction
    fun verify() {
        val enabledRules = rules.get().filter { it.isEnabled }
        tool.verify(enabledRules, resultFile.get().asFile, context())
    }

}