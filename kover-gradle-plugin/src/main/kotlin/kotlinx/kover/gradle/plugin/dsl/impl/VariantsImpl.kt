/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.dsl.impl

import kotlinx.kover.gradle.plugin.commons.KoverIllegalConfigException
import kotlinx.kover.gradle.plugin.commons.TOTAL_VARIANT_NAME
import kotlinx.kover.gradle.plugin.dsl.*
import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory
import org.gradle.kotlin.dsl.newInstance
import javax.inject.Inject

internal abstract class KoverVariantsRootConfigImpl @Inject constructor(private val objects: ObjectFactory) :
    KoverVariantConfigImpl(objects), KoverVariantsRootConfig {
    internal val byName: MutableMap<String, KoverVariantFactoryConfigImpl> = mutableMapOf()

    init {
        classes.excludeJava.convention(false)
        classes.excludedSourceSets.convention(emptySet())

        instrumentation.disableAll.set(false)
        instrumentation.excludedClasses.addAll(emptySet())

        testTasks.excluded.addAll(emptySet())
    }

    override fun named(variantName: String, block: Action<KoverVariantFactoryConfig>) {
        if (variantName == TOTAL_VARIANT_NAME) {
            throw KoverIllegalConfigException("TODO")
        }

        val variantConfig = byName.getOrPut(variantName) {
            create()
        }

        block.execute(variantConfig)
    }

    override fun total(block: Action<KoverVariantConfig>) {
        val variantConfig = byName.getOrPut(TOTAL_VARIANT_NAME) {
            create()
        }

        block.execute(variantConfig)
    }

    internal fun create(): KoverVariantFactoryConfigImpl {
        val variantConfig = objects.newInstance<KoverVariantFactoryConfigImpl>()

        // inherit from root configs
        variantConfig.classes.excludeJava.set(this.classes.excludeJava)
        variantConfig.classes.excludedSourceSets.addAll(this.classes.excludedSourceSets)

        variantConfig.instrumentation.disableAll.set(instrumentation.disableAll)
        variantConfig.instrumentation.excludedClasses.addAll(instrumentation.excludedClasses)

        variantConfig.testTasks.excluded.addAll(testTasks.excluded)
        return variantConfig
    }
}

internal abstract class KoverVariantConfigImpl @Inject constructor(objects: ObjectFactory) : KoverVariantConfig {
    internal val classes: KoverVariantClasses = objects.newInstance()
    internal val instrumentation: KoverVariantInstrumentation = objects.newInstance()
    internal val testTasks: KoverVariantTestTasks = objects.newInstance()

    override fun classes(block: Action<KoverVariantClasses>) {
        block.execute(classes)
    }

    override fun instrumentation(block: Action<KoverVariantInstrumentation>) {
        block.execute(instrumentation)
    }

    override fun testTasks(block: Action<KoverVariantTestTasks>) {
        block.execute(testTasks)
    }
}

internal abstract class KoverVariantFactoryConfigImpl @Inject constructor(private val objects: ObjectFactory) :
    KoverVariantConfigImpl(objects), KoverVariantFactoryConfig {
    // variant name -> optionality
    internal val variantNamesMap: MutableMap<String, Boolean> = mutableMapOf()
    internal val androidVariants: MutableList<KoverAndroidVariantBuilderImpl> = mutableListOf()

    override fun add(vararg variantNames: String, optional: Boolean) {
        for (variantName in variantNames) {
            if (variantName in variantNamesMap && variantNamesMap[variantName] != optional) {
                // TODO
                throw KoverIllegalConfigException("TODO: message")
            }
            variantNamesMap[variantName] = optional
        }
    }

    override fun addAndroid(optional: Boolean, block: Action<KoverAndroidVariantConfig>) {
        val builder = objects.newInstance<KoverAndroidVariantBuilderImpl>()
        block.execute(builder)
        androidVariants += builder
    }
}

internal abstract class KoverAndroidVariantBuilderImpl @Inject constructor(private val objects: ObjectFactory) :
    KoverAndroidVariantConfig {
    internal val buildVariants: MutableSet<String> = mutableSetOf()
    internal val buildTypes: MutableList<KoverAndroidBuildTypeConfigImpl> = mutableListOf()

    override fun buildVariant(buildVariantName: String) {
        buildVariants += buildVariantName
    }

    override fun buildType(buildTypeName: String, block: Action<KoverAndroidBuildTypeConfig>) {
        val buildType = objects.newInstance<KoverAndroidBuildTypeConfigImpl>()
        block.execute(buildType)
        buildTypes += buildType
    }
}

internal abstract class KoverAndroidBuildTypeConfigImpl : KoverAndroidBuildTypeConfig {
    internal val flavors: MutableSet<String> = mutableSetOf()

    override fun allFlavors() {
        flavors += ""
    }

    override fun flavors(vararg flavorName: String) {
        if (flavorName.contains("")) {
            TODO()
        }
        flavors.addAll(flavorName)
    }
}