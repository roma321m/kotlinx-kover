/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.dsl.impl

import kotlinx.kover.gradle.plugin.dsl.KoverExtension
import kotlinx.kover.gradle.plugin.dsl.KoverReportConfig
import kotlinx.kover.gradle.plugin.dsl.KoverVariantsRootConfig
import kotlinx.kover.gradle.plugin.dsl.KoverVersions.JACOCO_TOOL_DEFAULT_VERSION
import kotlinx.kover.gradle.plugin.dsl.shortcuts.KoverApplyConfig
import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory
import org.gradle.kotlin.dsl.newInstance
import javax.inject.Inject

@Suppress("LeakingThis")
internal abstract class KoverExtensionImpl @Inject constructor(objects: ObjectFactory): KoverExtension {
    internal val variants: KoverVariantsRootConfigImpl = objects.newInstance()
    internal val reports: KoverReportConfigImpl = objects.newInstance()

    init {
        useJacoco.set(false)
        jacocoVersion.set(JACOCO_TOOL_DEFAULT_VERSION)
    }

    override fun variants(block: Action<KoverVariantsRootConfig>) {
        block.execute(variants)
    }

    override fun reports(block: Action<KoverReportConfig>) {
        block.execute(reports)
    }

    override fun applyAllProjects(block: Action<KoverApplyConfig>) {
        TODO("Not yet implemented")
    }
}