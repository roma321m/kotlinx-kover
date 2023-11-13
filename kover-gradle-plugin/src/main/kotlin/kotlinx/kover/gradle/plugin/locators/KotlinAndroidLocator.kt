/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.locators

import kotlinx.kover.gradle.plugin.sources.AndroidVariantSource
import kotlinx.kover.gradle.plugin.sources.VariantSources
import kotlinx.kover.gradle.plugin.commons.*
import kotlinx.kover.gradle.plugin.util.*
import org.gradle.api.*

/*
Since the Kover and Android plug-ins can be in different class loaders (declared in different projects), the plug-ins are stored in a single instance in the loader of the project where the plug-in was used for the first time.
Because of this, Kover may not have direct access to the Android plugin classes, and variables and literals of this types cannot be declared .

To work around this limitation, working with objects is done through reflection, using a dynamic Gradle wrapper.
 */
internal fun Project.locateKotlinAndroidVariants(): VariantSources {
    val kotlinExtension = project.getKotlinExtension()
    val android = locateAndroidVariants(kotlinExtension)
    return VariantSources(null, android)
}

private fun Project.locateAndroidVariants(kotlinExtension: DynamicBean): List<AndroidVariantSource> {
    val androidExtension = project.extensions.findByName("android")?.bean()
        ?: throw KoverCriticalException("Kover requires extension with name 'android' for project '${project.path}' since it is recognized as Kotlin/Android project")

    val kotlinTarget = kotlinExtension["target"]

    return project.androidCompilationKits(androidExtension, kotlinTarget)
}
