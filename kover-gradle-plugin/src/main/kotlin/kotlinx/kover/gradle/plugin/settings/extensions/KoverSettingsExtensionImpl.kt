/*
 * Copyright 2017-2024 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.settings.extensions

import kotlinx.kover.gradle.plugin.dsl.KoverSettingsExtension
import kotlinx.kover.gradle.plugin.dsl.MainReport
import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.kotlin.dsl.newInstance
import javax.inject.Inject

@Suppress("LeakingThis")
internal abstract class KoverSettingsExtensionImpl @Inject constructor(
    objects: ObjectFactory
) : KoverSettingsExtension {
    abstract val coverageIsEnabled: Property<Boolean>

    override val main: MainReport = objects.newInstance<MainReport>()

    //    abstract override val variants: NamedDomainObjectContainer<VariantI>

    init {
        coverageIsEnabled.convention(false)
    }

    override fun enableCoverage() {
        coverageIsEnabled.set(true)
    }

    override fun main(action: Action<MainReport>) {
        action.execute(main)
    }
}