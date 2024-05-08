/*
 * Copyright 2017-2024 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.settings.extensions

internal abstract class KoverProjectExtensionImpl {
    internal abstract var coverageIsEnabled: Boolean
}