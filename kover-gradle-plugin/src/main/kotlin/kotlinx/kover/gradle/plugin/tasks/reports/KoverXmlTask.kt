/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.tasks.reports

import kotlinx.kover.gradle.plugin.dsl.tasks.KoverXmlReport
import org.gradle.api.file.*
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import javax.inject.Inject

@CacheableTask
internal abstract class KoverXmlTask @Inject constructor(@get:Internal override val variantName: String) : AbstractKoverReportTask(), KoverXmlReport {
    @get:OutputFile
    internal abstract val reportFile: RegularFileProperty

    @get:Input
    abstract val title: Property<String>

    @TaskAction
    fun generate() {
        val xmlFile = reportFile.get().asFile
        xmlFile.parentFile.mkdirs()
        tool.get().xmlReport(xmlFile, title.get(), context())
    }
}
