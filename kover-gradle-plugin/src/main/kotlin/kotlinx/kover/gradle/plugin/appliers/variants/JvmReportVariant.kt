/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.appliers.variants

import kotlinx.kover.gradle.plugin.commons.ReportsVariantType
import kotlinx.kover.gradle.plugin.commons.VariantNameAttr
import kotlinx.kover.gradle.plugin.commons.VariantOriginAttr
import kotlinx.kover.gradle.plugin.dsl.KoverNames.JVM_VARIANT_NAME
import kotlinx.kover.gradle.plugin.dsl.impl.KoverVariantConfigImpl
import kotlinx.kover.gradle.plugin.sources.JvmVariantSource
import kotlinx.kover.gradle.plugin.tools.CoverageTool
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.named

internal class JvmReportVariant(
    project: Project,
    toolProvider: Provider<CoverageTool>,
    koverBucketConfiguration: Configuration,
    variantSource: JvmVariantSource,
    variantConfig: KoverVariantConfigImpl
) : AbstractReportVariant(project, JVM_VARIANT_NAME, toolProvider, koverBucketConfiguration, variantConfig) {
    init {
        producerConfiguration.configure {
            attributes {
                attribute(VariantOriginAttr.ATTRIBUTE, project.objects.named(ReportsVariantType.JVM.name))
            }
        }

        consumerConfiguration.configure {
            attributes {
                // depends on JVM-only variants
                attribute(VariantOriginAttr.ATTRIBUTE, project.objects.named(ReportsVariantType.JVM.name))
                attribute(VariantNameAttr.ATTRIBUTE, project.objects.named(variantName))
            }
        }

        fromSource(variantSource) { compilationName ->
            compilationName !in variantConfig.classes.excludedSourceSets.get()
                    && compilationName != "test"
        }
    }

}