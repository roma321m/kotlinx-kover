/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.appliers.variants

import kotlinx.kover.gradle.plugin.commons.ReportsVariantType
import kotlinx.kover.gradle.plugin.commons.VariantNameAttr
import kotlinx.kover.gradle.plugin.commons.VariantOriginAttr
import kotlinx.kover.gradle.plugin.dsl.impl.KoverVariantFactoryConfigImpl
import kotlinx.kover.gradle.plugin.tools.CoverageTool
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.named

internal open class CustomReportVariant(
    project: Project,
    variantName: String,
    toolProvider: Provider<CoverageTool>,
    koverBucketConfiguration: Configuration,
    variantConfig: KoverVariantFactoryConfigImpl
): AbstractReportVariant(project, variantName, toolProvider, koverBucketConfiguration, variantConfig) {
    init {
        producerConfiguration.configure {
            attributes {
                attribute(VariantOriginAttr.ATTRIBUTE, project.objects.named(ReportsVariantType.CUSTOM.name))
            }
        }

        consumerConfiguration.configure {
            attributes {
                // depends on custom variants
                attribute(VariantOriginAttr.ATTRIBUTE, project.objects.named(ReportsVariantType.CUSTOM.name))
                attribute(VariantNameAttr.ATTRIBUTE, project.objects.named(variantName))
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
