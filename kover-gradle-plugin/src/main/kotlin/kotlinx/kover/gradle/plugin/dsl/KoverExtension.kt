/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.dsl

import kotlinx.kover.gradle.plugin.dsl.KoverVersions.JACOCO_TOOL_DEFAULT_VERSION
import kotlinx.kover.gradle.plugin.dsl.shortcuts.KoverApplyConfig
import org.gradle.api.Action
import org.gradle.api.provider.Property

public interface KoverExtension {

    /**
     * JaCoCo Coverage Tool with default version [JACOCO_TOOL_DEFAULT_VERSION].
     */
    public fun useJacoco() {
        useJacoco.set(true)
    }

    /**
     * Coverage Tool by [JaCoCo](https://www.jacoco.org/jacoco/).
     */
    public fun useJacoco(version: String) {
        useJacoco.set(true)
        jacocoVersion.set(version)
    }

    public val useJacoco: Property<Boolean>

    public val jacocoVersion: Property<String>

    public fun variants(block: Action<KoverVariantsRootConfig>)

    public fun reports(block: Action<KoverReportConfig>)

    public fun applyInAllProjects(block: Action<KoverApplyConfig>)
}
