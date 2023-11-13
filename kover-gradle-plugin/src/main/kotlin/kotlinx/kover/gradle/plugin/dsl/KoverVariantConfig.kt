/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.dsl

import org.gradle.api.Action
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty


public interface KoverVariantsRootConfig: KoverVariantConfig {

    public fun named(variantName: String, block: Action<KoverVariantFactoryConfig>)

    public fun total(block: Action<KoverVariantConfig>)
}

public interface KoverVariantConfig {
    public fun classes(block: Action<KoverVariantClasses>)
    public fun instrumentation(block: Action<KoverVariantInstrumentation>)
    public fun testTasks(block: Action<KoverVariantTestTasks>)
}

public interface KoverVariantClasses {
    public val excludeJava: Property<Boolean>
    public val excludedSourceSets: SetProperty<String>
}

public interface KoverVariantInstrumentation {
    public val disableAll: Property<Boolean>
    public val excludedClasses: SetProperty<String>
}

public interface KoverVariantTestTasks {
    public val excluded: SetProperty<String>
}

public interface KoverVariantFactoryConfig: KoverVariantConfig {
    // TODO check optional in groovy
    public fun add(vararg variantNames: String, optional: Boolean = false)


    public fun addAndroid(optional: Boolean = false, block: Action<KoverAndroidVariantConfig>)
}

public interface KoverAndroidVariantConfig {
    fun buildVariant(buildVariantName: String)

    fun buildType(buildTypeName: String, block: Action<KoverAndroidBuildTypeConfig>)
}

public interface KoverAndroidBuildTypeConfig {
    fun flavors(vararg flavorName: String)

    fun allFlavors()
}
