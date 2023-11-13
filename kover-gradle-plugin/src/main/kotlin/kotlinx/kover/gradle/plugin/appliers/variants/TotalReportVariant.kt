/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.appliers.variants

import kotlinx.kover.gradle.plugin.commons.ReportsVariantType
import kotlinx.kover.gradle.plugin.commons.TOTAL_VARIANT_NAME
import kotlinx.kover.gradle.plugin.commons.VariantOriginAttr
import kotlinx.kover.gradle.plugin.dsl.impl.KoverVariantConfigImpl
import kotlinx.kover.gradle.plugin.tools.CoverageTool
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.named

internal class TotalReportVariant(
    project: Project,
    toolProvider: Provider<CoverageTool>,
    variantConfig: KoverVariantConfigImpl
): AbstractReportVariant(project, TOTAL_VARIANT_NAME, toolProvider, null, variantConfig) {
    init {
        producerConfiguration.configure {
            attributes {
                attribute(VariantOriginAttr.ATTRIBUTE, project.objects.named(ReportsVariantType.TOTAL.name))
            }
        }
    }

    fun mergeWith(otherVariant: AbstractReportVariant) {
        artifactGenTask.configure {
            additionalArtifacts.from(
                otherVariant.artifactGenTask.map { task -> task.artifactFile },
                otherVariant.consumerConfiguration
            )
            dependsOn(otherVariant.artifactGenTask, otherVariant.consumerConfiguration)
        }
    }
}