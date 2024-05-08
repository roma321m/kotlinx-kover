/*
 * Copyright 2017-2024 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.dsl

import org.gradle.api.provider.SetProperty

@KoverGradlePluginDsl
public interface KoverLocalProjectExtension {
    val disabledTasks: SetProperty<String>
}