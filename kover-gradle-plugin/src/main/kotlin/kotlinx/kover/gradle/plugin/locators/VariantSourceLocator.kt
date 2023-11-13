/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.locators

import kotlinx.kover.gradle.plugin.sources.VariantSources
import kotlinx.kover.gradle.plugin.commons.*
import kotlinx.kover.gradle.plugin.util.DynamicBean
import kotlinx.kover.gradle.plugin.util.bean
import org.gradle.api.*

internal class VariantSourceLocator(
    private val project: Project,
    private val callback: (sources: VariantSources) -> Unit
) {
    private val reasons: MutableSet<String> = mutableSetOf()
    private var finilized: Boolean = false

    init {
        enqueue("no-plugin")

        project.pluginManager.withPlugin(KOTLIN_PLUGIN_ID) {
            enqueue(KOTLIN_PLUGIN_ID)
        }
        project.pluginManager.withPlugin(KOTLIN_MULTIPLATFORM_PLUGIN_ID) {
            enqueue(KOTLIN_MULTIPLATFORM_PLUGIN_ID)
        }
        project.pluginManager.withPlugin(KOTLIN_ANDROID_PLUGIN_ID) {
            enqueue(KOTLIN_ANDROID_PLUGIN_ID)
        }
        project.pluginManager.withPlugin(ANDROID_APP_PLUGIN_ID) {
            enqueueAndroid(ANDROID_APP_PLUGIN_ID)
        }
        project.pluginManager.withPlugin(ANDROID_LIB_PLUGIN_ID) {
            enqueueAndroid(ANDROID_LIB_PLUGIN_ID)
        }
        project.pluginManager.withPlugin(ANDROID_DYNAMIC_PLUGIN_ID) {
            enqueueAndroid(ANDROID_DYNAMIC_PLUGIN_ID)
        }
    }

    // TODO application of any of plugin

    private fun enqueueAndroid(reason: String) {
        val androidComponents = project.extensions.findByName("androidComponents")?.bean()
            ?: throw KoverCriticalException("Kover requires extension with name 'androidComponents' for project '${project.path}' since it is recognized as Kotlin+Android project")

        val callback = Action<Any> {
            enqueue(reason)
        }

        if (androidComponents.hasFunction("finalizeDsl", callback)) {
            /*
            Assumption: `finalizeDsl` is called in the `afterEvaluate` action, in which build variants are created.
            Therefore,  if an action is added to the queue inside it, it will be executed only after variants are created
             */
            androidComponents("finalizeDsl", callback)
        } else {
            // for old versions < 7.0 an action is added to the AAA queue.
            // Since this code is executed after the applying of AGP, there is a high probability that the action will fall into the `afterEvaluate` queue after the actions of the AGP
            enqueue(reason)
        }
    }

    private fun enqueue(reason: String) {
        if (finilized) {
            TODO()
        }

        reasons += reason

        project.afterEvaluate {
            if (reasons.isEmpty()) {
                TODO() // reason
            }

            reasons.remove(reason)
            if (reasons.isEmpty()) {
                finalize()
            }
        }
    }

    private fun finalize() {
        finilized = true

        val variants = if (project.pluginManager.hasPlugin(KOTLIN_PLUGIN_ID)) {
            project.locateKotlinJvmVariants()
        } else if (project.pluginManager.hasPlugin(KOTLIN_ANDROID_PLUGIN_ID)) {
            project.locateKotlinAndroidVariants()
        } else if (project.pluginManager.hasPlugin(KOTLIN_MULTIPLATFORM_PLUGIN_ID)) {
            project.locateKotlinMultiplatformVariants()
        } else {
            VariantSources(null, emptyList())
        }

        callback(variants)
    }

}


internal fun Project.getKotlinExtension(): DynamicBean {
    return extensions.findByName("kotlin")?.bean()
        ?: throw KoverCriticalException("Kover requires extension with name 'kotlin' for project '${project.path}' since it is recognized as Kotlin/Multiplatform project")
}

