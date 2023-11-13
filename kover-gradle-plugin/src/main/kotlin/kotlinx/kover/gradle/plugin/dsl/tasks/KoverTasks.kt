/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.dsl.tasks

interface KoverReport {
    // TODO type: total, jvm, android, ...
}

interface KoverXmlReport: KoverReport

interface KoverHtmlReport: KoverReport

interface KoverLogReport: KoverReport

interface KoverVerifyReport: KoverReport

interface KoverBinaryReport: KoverReport
